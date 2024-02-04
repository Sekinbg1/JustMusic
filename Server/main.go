package main

import (
	"bytes"
	"compress/gzip"
	"context"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"github.com/quic-go/quic-go"
	"os"
	"time"
)

type MsgType uint8

const (
	MSGString MsgType = 0
	MSGSalt   MsgType = 1
	MSGLogin  MsgType = 2
	MSGClass  MsgType = 3
	MSGBank   MsgType = 4
	MSGSong   MsgType = 5
)

type User struct {
	Conn    quic.Connection
	stream  quic.Stream
	logined bool
	Account string
	Name    string
}

var (
	users  = make([]User, 0)
	isOpen = true

	song, _ = os.ReadFile("Sincerely.mid")
	classes = Classes{
		ClassesNames: []string{"test"},
		Classes: map[string]Class{
			"test": {
				Name:      "test",
				BankNames: []string{"test"},
				Banks: map[string]Bank{
					"test": {
						Name:          "test",
						Creator:       "test",
						Info:          "test",
						OnlineAccount: "test",
						SongNames:     []string{"test"},
						Songs: map[string]Song{
							"test": {
								Name:    "test",
								Info:    "test",
								Creator: "test",
								Data:    song,
							},
						},
					},
				},
			},
		},
	}
)

func main() {
	cert, err := tls.LoadX509KeyPair("server.crt", "private.key")
	tlsConfig := &tls.Config{
		Certificates:       []tls.Certificate{cert},
		InsecureSkipVerify: true, // For testing purposes only
		NextProtos:         []string{"quic-JPServer"},
	}
	quicConfig := &quic.Config{
		Allow0RTT:       true,
		EnableDatagrams: false,
		KeepAlivePeriod: time.Hour * 24,
	}
	listener, err := quic.ListenAddr("0.0.0.0:1130", tlsConfig, quicConfig)
	if err != nil {
		fmt.Println(err)
	}
	defer listener.Close()
	for {
		accept, err := listener.Accept(context.Background())
		if err != nil {
			fmt.Println(err)
		}
		u := User{Conn: accept, logined: false}
		go u.handleClient()
		users = append(users, u)
	}
}

type Class struct {
	Name      string          `json:"name"`
	BankNames []string        `json:"bankNames"`
	Banks     map[string]Bank `json:"banks"`
}
type Classes struct {
	ClassesNames []string         `json:"classesNames"`
	Classes      map[string]Class `json:"classes"`
}
type ClassesJSON struct {
	ClassesNames []string                  `json:"classesNames"`
	Classes      map[string]ClassBanksJSON `json:"classes"`
}
type ClassBanksJSON struct {
	Name      string                   `json:"name"`
	BankNames []string                 `json:"bankNames"`
	Banks     map[string]ClassBankJSON `json:"banks"`
}
type ClassBankJSON struct {
	Name          string `json:"name"`
	Creator       string `json:"creator"`
	Info          string `json:"info"`
	OnlineAccount string `json:"onlineAccount"`
}
type BankJSON struct {
	Name          string   `json:"name"`
	Creator       string   `json:"creator"`
	Info          string   `json:"info"`
	OnlineAccount string   `json:"onlineAccount"`
	SongNames     []string `json:"songNames"`
}
type SongInfoJSON struct {
	Name    string `json:"name"`
	Info    string `json:"info,"`
	Creator string `json:"creator"`
}
type Bank struct {
	Name          string          `json:"name"`
	Creator       string          `json:"creator"`
	Info          string          `json:"info"`
	OnlineAccount string          `json:"onlineAccount"`
	SongNames     []string        `json:"songNames"`
	Songs         map[string]Song `json:"songs"`
}
type Song struct {
	Name    string `json:"name"`
	Info    string `json:"info,"`
	Creator string `json:"creator"`
	Data    []byte `json:"data"`
}

func (u User) OnMessage(message []byte) {
	bw := bytes.NewReader(message)
	gr, err := gzip.NewReader(bw)
	if err != nil {
		fmt.Println(err)
		return
	}
	defer gr.Close()
	buf := new(bytes.Buffer)
	_, err = buf.ReadFrom(gr)
	if err != nil {
		fmt.Println(err)
		return
	}
	message = buf.Bytes()
	c := MsgType(message[0])
	message = message[1:]
	fmt.Println("r", c, string(message))
	switch c {
	case MSGString:
		if isOpen {
			u.SendMessage(MSGString, []byte{0})
		} else {
			msg := []byte("服务器正在维护中")
			u.SendMessage(MSGString, append([]byte{1}, msg...))
		}
		break
	case MSGSalt:
		u.SendMessage(MSGSalt, []byte("123"))
		break
	case MSGLogin:
		u.SendMessage(MSGLogin, []byte("欢迎回来"))
		break
	case MSGClass:
		classesJson := ClassesJSON{ClassesNames: classes.ClassesNames, Classes: map[string]ClassBanksJSON{}}
		for _, c := range classes.Classes {
			classesJson.Classes[c.Name] = ClassBanksJSON{Name: c.Name, BankNames: c.BankNames, Banks: map[string]ClassBankJSON{}}
			for _, b := range c.Banks {
				classesJson.Classes[c.Name].Banks[b.Name] = ClassBankJSON{Name: b.Name, Creator: b.Creator, Info: b.Info, OnlineAccount: b.OnlineAccount}
			}
		}
		d, _ := json.Marshal(classesJson)
		u.SendMessage(MSGClass, d)
		break
	case MSGBank:
		j := struct {
			Class string `json:"class"`
			Bank  string `json:"bank"`
		}{}
		json.Unmarshal(message, &j)
		bank := classes.Classes[j.Class].Banks[j.Bank]
		bankJson := BankJSON{
			Name:          bank.Name,
			Creator:       bank.Info,
			Info:          bank.Info,
			OnlineAccount: bank.OnlineAccount,
			SongNames:     bank.SongNames}
		d, _ := json.Marshal(bankJson)
		u.SendMessage(MSGBank, d)
		break
	case MSGSong:
		j := struct {
			Class string `json:"class"`
			Bank  string `json:"bank"`
			Song  string `json:"song"`
		}{}
		json.Unmarshal(message, &j)
		song := classes.Classes[j.Class].Banks[j.Bank].Songs[j.Song]
		d, _ := json.Marshal(song)
		u.SendMessage(MSGSong, d)
		break
	}
}

func (u User) login() {
	u.logined = true
}
func (u *User) SendMessage(t MsgType, data []byte) {
	fmt.Println("s", t, string(data))
	buf := new(bytes.Buffer)
	gw, err := gzip.NewWriterLevel(buf, gzip.BestCompression)
	_, err = gw.Write([]byte{byte(t)})
	_, err = gw.Write(data)
	if err != nil {
		fmt.Println(err)
		return
	}
	gw.Close()
	u.stream.Write(int2byte(len(buf.Bytes())))
	u.stream.Write(buf.Bytes())
}
func (u User) Close() {
	for i, u1 := range users {
		if u1 == u {
			users = append(users[:i], users[i+1:]...)
		}
	}
}
func (u *User) handleClient() {
	defer u.Close()
	stream, err := u.Conn.AcceptStream(context.Background())
	u.stream = stream
	if err != nil {
		fmt.Println(err)
	}
	defer u.stream.Close()
	lenBuffer := make([]byte, 1)
	for {
		lenData := make([]byte, 0)
		_, err := u.stream.Read(lenBuffer)
		if err != nil {
			break
		}
		for lenBuffer[0] > 0x7f {
			lenData = append(lenData, lenBuffer[0])
			u.stream.Read(lenBuffer)
		}
		lenData = append(lenData, lenBuffer[0])
		lenr := bytes2int(lenData)
		data := make([]byte, lenr)
		readlen := 0
		for readlen < lenr {
			rl, err := u.stream.Read(data[readlen:])
			if err != nil {
				break
			}
			readlen += rl
		}
		u.OnMessage(data)
	}
	fmt.Println(u.Conn.RemoteAddr(), ":", "closed")
}
func bytes2int(data []byte) int {
	length := 0
	for i := 0; i < len(data); i++ {
		length += (int(data[i]) & 0x7f) << (i * 7)
	}
	return length
}
func int2byte(data int) []byte {
	result := make([]byte, 0)
	for data > 0x7f {
		result = append(result, byte(data)|0x80)
		data >>= 7
	}
	result = append(result, byte(data))
	return result
}
