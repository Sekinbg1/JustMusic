package Client

import (
	"bytes"
	"compress/gzip"
	"context"
	"crypto/tls"
	"fmt"
	"github.com/quic-go/quic-go"
	"log"
	"os"
	"time"
)

type ConnectClient struct {
	stream    quic.Stream
	conn      quic.Connection
	listeners map[int]OnMessageListener
}

func (c *ConnectClient) Init() {
	os.Setenv("QUIC_GO_DISABLE_ECN", "true")
	c.listeners = make(map[int]OnMessageListener)
}

func (c *ConnectClient) Connect(addr string, protos string) {
	tlsConf := &tls.Config{
		InsecureSkipVerify: true,
		NextProtos:         []string{protos},
	}
	quicConf := &quic.Config{
		Allow0RTT: true,
	}
	con, _ := context.WithTimeout(context.Background(), time.Second)
	connection, err := quic.DialAddr(con, addr, tlsConf, quicConf)
	if err != nil {
		c.Close()
		log.Println(err)
		return
	}
	c.stream, err = connection.OpenStream()
	c.conn = connection
	if err != nil {
		c.Close()
		log.Println(err)
		return
	}
	go c.listen()
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
func (c *ConnectClient) Close() {
	for _, l := range c.listeners {
		l.OnEnd()
	}
	if c.stream != nil {
		c.stream.Close()
	}
	c.stream = nil
}
func (c *ConnectClient) IsConnected() bool {
	return c.stream != nil
}

type OnMessageListener interface {
	OnMessage(data []byte)
	OnEnd()
}

func (c *ConnectClient) AddOnMessageListener(code uint8, l OnMessageListener) {
	c.listeners[int(code)] = l
}

func (c *ConnectClient) listen() {
	defer c.Close()
	for {
		lenBuffer := make([]byte, 1)
		lenData := make([]byte, 0)
		_, err := c.stream.Read(lenBuffer)
		if err != nil {
			log.Println(err)
			return
		}
		for lenBuffer[0] > 0x7f {
			lenData = append(lenData, lenBuffer[0])
			c.stream.Read(lenBuffer)
		}
		lenData = append(lenData, lenBuffer[0])
		lenr := bytes2int(lenData)
		data := make([]byte, lenr)
		readlen := 0
		for readlen < lenr {
			rl, err := c.stream.Read(data[readlen:])
			if err != nil {
				log.Println(err)
				return
			}
			readlen += rl
		}
		bw := bytes.NewReader(data)
		gr, err := gzip.NewReader(bw)
		if err != nil {
			log.Println(err)
			return
		}
		buf := new(bytes.Buffer)
		_, err = buf.ReadFrom(gr)
		gr.Close()
		if err != nil {
			log.Println(err)
			return
		}
		if c.listeners[int(buf.Bytes()[0])] != nil {
			c.listeners[int(buf.Bytes()[0])].OnMessage(buf.Bytes()[1:])
			fmt.Println("onMessage", buf.Bytes()[0], string(buf.Bytes()[1:]))
		}
	}
}

func (c *ConnectClient) SendMessage(t uint8, data []byte) {
	if c.stream == nil {
		return
	}
	fmt.Println("send:", t, string(data))
	buf := new(bytes.Buffer)
	gw, err := gzip.NewWriterLevel(buf, gzip.BestCompression)
	_, err = gw.Write([]byte{t})
	_, err = gw.Write(data)
	if err != nil {
		fmt.Println(err)
		return
	}
	gw.Close()
	c.stream.Write(int2byte(len(buf.Bytes())))
	c.stream.Write(buf.Bytes())
}
