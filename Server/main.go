package main

import (
	"bytes"
	"compress/gzip"
	"context"
	"crypto/tls"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/layout"
	"fyne.io/fyne/v2/widget"
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
	uWindow = container.NewVBox()

	hbox    = container.NewAdaptiveGrid(2)
	classes = Classes{
		ClassesNames: &[]string{"test", "test2"},
		Classes: map[string]*Class{
			"test": {
				Name:      "test",
				BankNames: &[]string{"test", "test2"},
				Banks: map[string]*Bank{
					"test": {
						Name:          "test",
						Creator:       "test",
						Info:          "test",
						OnlineAccount: "test",
						SongNames:     &[]string{"test"},
						Songs: map[string]Song{
							"test": {
								Name:    "test",
								Info:    "test",
								Creator: "test",
								Data:    song,
							},
						},
					},
					"test2": {
						Name:          "test2",
						Creator:       "test",
						Info:          "test",
						OnlineAccount: "test",
						SongNames:     &[]string{"test"},
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
			"test2": {
				Name:      "test2",
				BankNames: &[]string{"test", "test2"},
				Banks: map[string]*Bank{
					"test": {
						Name:          "test",
						Creator:       "test",
						Info:          "test",
						OnlineAccount: "test",
						SongNames:     &[]string{"test"},
						Songs: map[string]Song{
							"test": {
								Name:    "test",
								Info:    "test",
								Creator: "test",
								Data:    song,
							},
						},
					},
					"test2": {
						Name:          "test2",
						Creator:       "test",
						Info:          "test",
						OnlineAccount: "test",
						SongNames:     &[]string{"test"},
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

func server() {

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
func addSong(name, info, author, creator string, data []byte, class *Bank) {
	*class.SongNames = append(*class.SongNames, name)
	class.Songs[name] = Song{
		Name:    name,
		Author:  author,
		Creator: creator,
		Info:    info,
		Data:    data,
	}
}
func addSongB64(name, info, author, creator string, data string, class *Bank) {
	decodeString, err := base64.StdEncoding.DecodeString(data)
	if err != nil {
		return
	}
	*class.SongNames = append(*class.SongNames, name)
	class.Songs[name] = Song{
		Name:    name,
		Author:  author,
		Creator: creator,
		Info:    info,
		Data:    decodeString,
	}
}
func addSongPoint(box *fyne.Container, labels *Bank) {
	box.Add(container.NewHBox(widget.NewButton("+", func() {
		uWindow.RemoveAll()
		name := widget.NewEntry()
		creator := widget.NewEntry()
		info := widget.NewEntry()
		author := widget.NewEntry()
		data := widget.NewMultiLineEntry()
		uWindow.Add(widget.NewForm(
			&widget.FormItem{Text: "名称:", Widget: name},
			&widget.FormItem{Text: "创建者:", Widget: creator},
			&widget.FormItem{Text: "信息:", Widget: info},
			&widget.FormItem{Text: "作者:", Widget: author},
			&widget.FormItem{Text: "数据(base64):", Widget: data},
		))

		uWindow.Add(widget.NewButton("添加", func() {
			addSongB64(name.Text, info.Text, author.Text, creator.Text, data.Text, labels)
			buildUI()
		}))
	})))
	for _, className := range *labels.SongNames {
		class := labels.Songs[className]
		box.Add(widget.NewButton(class.Name, func() {
			uWindow.RemoveAll()
			uWindow.Add(widget.NewLabel("类型：song"))
			uWindow.Add(widget.NewLabel("名称：" + class.Name))
			uWindow.Add(widget.NewLabel("作者：" + class.Author))
			uWindow.Add(widget.NewLabel("创建者：" + class.Creator))
			uWindow.Add(widget.NewLabel("介绍：" + class.Info))
			uWindow.Add(widget.NewButton("删除", func() {
				s := []string{}
				for _, n := range *labels.SongNames {
					if n != class.Name {
						s = append(s, n)
					}
				}
				*labels.SongNames = s
				delete(labels.Songs, class.Name)
				buildUI()
			}))
		}))
	}
}
func addBank(name, creator, info, onlineAccount string, class *Class) {
	*class.BankNames = append(*class.BankNames, name)
	class.Banks[name] = &Bank{
		Name:          name,
		Creator:       creator,
		Info:          info,
		OnlineAccount: onlineAccount,
		Expand:        new(bool),
		SongNames:     &[]string{},
		Songs:         map[string]Song{},
	}
}
func addBankPoint(box *fyne.Container, labels *Class) {
	box.Add(container.NewHBox(widget.NewButton("+", func() {
		uWindow.RemoveAll()
		name := widget.NewEntry()
		creator := widget.NewEntry()
		info := widget.NewEntry()
		onlineAccount := widget.NewEntry()
		uWindow.Add(widget.NewForm(
			&widget.FormItem{Text: "名称:", Widget: name},
			&widget.FormItem{Text: "创建者:", Widget: creator},
			&widget.FormItem{Text: "信息:", Widget: info},
			&widget.FormItem{Text: "在线账户:", Widget: onlineAccount},
		))

		uWindow.Add(widget.NewButton("添加", func() {
			addBank(name.Text, creator.Text, info.Text, onlineAccount.Text, labels)
			buildUI()
		}))
	})))
	for _, className := range *labels.BankNames {
		class := labels.Banks[className]
		l := container.NewHBox()
		v := container.NewVBox()
		bl := container.NewVBox()
		l.Add(bl)
		bt := widget.NewButton("v", nil)
		if class.Expand == nil {
			class.Expand = new(bool)
		}
		if !*class.Expand {
			v.Hide()
			bt.SetText(">")
		}
		bt.OnTapped = func() {
			if v.Visible() {
				*class.Expand = false
				bt.SetText(">")
				v.Hide()
			} else {
				*class.Expand = true
				bt.SetText("v")
				v.Show()
			}
		}
		bl.Add(bt)
		addSongPoint(v, class)
		vl := container.NewVBox()
		//finalpos := pos
		label := widget.NewButton(class.Name, func() {
			uWindow.RemoveAll()
			uWindow.Add(widget.NewLabel("类型：bank"))
			uWindow.Add(widget.NewLabel("名称：" + class.Name))
			uWindow.Add(widget.NewLabel("创建者" + class.Creator))
			uWindow.Add(widget.NewLabel("在线账号" + class.OnlineAccount))
			uWindow.Add(widget.NewLabel("介绍" + class.Info))
			uWindow.Add(widget.NewButton("删除", func() {
				s := []string{}
				for _, n := range *labels.BankNames {
					if n != class.Name {
						s = append(s, n)
					}
				}
				*labels.BankNames = s
				delete(labels.Banks, class.Name)
				buildUI()
			}))
		})
		vl.Add(label)
		l.Add(vl)
		l.Add(v)
		box.Add(l)
	}
}
func addClass(name string, classes *Classes) {
	*classes.ClassesNames = append(*classes.ClassesNames, name)
	classes.Classes[name] = &Class{
		Name:      name,
		Expand:    new(bool),
		BankNames: &[]string{},
		Banks:     map[string]*Bank{},
	}
}
func addClassPoint(box *fyne.Container, labels *Classes) {
	box.Add(container.NewHBox(widget.NewButton("+", func() {
		uWindow.RemoveAll()
		name := widget.NewEntry()
		uWindow.Add(widget.NewForm(
			&widget.FormItem{Text: "名称:", Widget: name},
		))
		uWindow.Add(widget.NewButton("添加", func() {
			addClass(name.Text, labels)
			buildUI()
		}))
	})))
	for _, className := range *labels.ClassesNames {
		class := labels.Classes[className]
		l := container.NewHBox()
		v := container.NewVBox()
		bl := container.NewVBox()
		l.Add(bl)
		bt := widget.NewButton("v", nil)
		if class.Expand == nil {
			class.Expand = new(bool)
		}
		if !*class.Expand {
			v.Hide()
			bt.SetText(">")
		}
		bt.OnTapped = func() {
			if v.Visible() {
				*class.Expand = false
				bt.SetText(">")
				v.Hide()
			} else {
				*class.Expand = true
				bt.SetText("v")
				v.Show()
			}
		}
		bl.Add(bt)
		addBankPoint(v, class)
		vb := container.NewVBox()
		label := widget.NewButton(class.Name, func() {
			uWindow.RemoveAll()
			uWindow.Add(widget.NewLabel("类型：class"))
			uWindow.Add(widget.NewLabel("名称：" + class.Name))
			uWindow.Add(widget.NewButton("删除", func() {
				s := []string{}
				for _, n := range *labels.ClassesNames {
					if n != class.Name {
						s = append(s, n)
					}
				}
				*labels.ClassesNames = s
				delete(labels.Classes, class.Name)
				buildUI()
			}))
		})
		vb.Add(label)
		l.Add(vb)
		l.Add(v)
		box.Add(l)
	}
}
func buildUI() {
	hbox.RemoveAll()
	uWindow.RemoveAll()
	vbox := container.New(layout.NewVBoxLayout())
	hbox.Add(vbox)
	hbox.Add(uWindow)
	hbox.Resize(fyne.NewSize(720, 480))
	vbox.Resize(fyne.NewSize(360, 480))
	addClassPoint(vbox, &classes)
}

func main() {
	go server()
	myApp := app.New()
	myApp.Settings().SetTheme(serverTheme)
	myWindow := myApp.NewWindow("服务器管理控制台")
	myWindow.Resize(fyne.Size{Height: 480, Width: 720})
	myWindow.SetFullScreen(false)
	buildUI()
	myWindow.SetContent(hbox)
	myWindow.ShowAndRun()
}

type Class struct {
	Name      string           `json:"name"`
	Expand    *bool            `json:"-"  default:"false"`
	BankNames *[]string        `json:"bankNames"`
	Banks     map[string]*Bank `json:"banks"`
}
type Classes struct {
	ClassesNames *[]string         `json:"classesNames"`
	Classes      map[string]*Class `json:"classes"`
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
	Expand        *bool           `json:"-" default:"new(bool)"`
	Creator       string          `json:"creator"`
	Info          string          `json:"info"`
	OnlineAccount string          `json:"onlineAccount"`
	SongNames     *[]string       `json:"songNames"`
	Songs         map[string]Song `json:"songs"`
}
type Song struct {
	Name    string `json:"name"`
	Info    string `json:"info,"`
	Author  string `json:"author"`
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
		classesJson := ClassesJSON{ClassesNames: *classes.ClassesNames, Classes: map[string]ClassBanksJSON{}}
		for _, c := range classes.Classes {
			classesJson.Classes[c.Name] = ClassBanksJSON{Name: c.Name, BankNames: *c.BankNames, Banks: map[string]ClassBankJSON{}}
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
			SongNames:     *bank.SongNames}
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
