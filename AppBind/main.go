package main

import (
	"AppBind/Client"
	"time"
)

func main() {
	client := Client.ConnectClient{}
	client.Connect("192.168.5.61:1130", "quic-JPServer")
	client.SendMessage([]byte{0x00, byte('h')})
	time.Sleep(time.Second)
}
