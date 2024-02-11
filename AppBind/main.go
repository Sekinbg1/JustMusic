package main

import (
	"AppBind/Client"
)

type name struct {
}

func (n name) OnEnd() {
}

func (name) OnMessage(data []byte) {

}
func main() {
	client := Client.ConnectClient{}
	client.Init()
	client.Connect("192.158.5.243:1130", "quic-JPServer")
	//l := name{}
	//client.AddOnMessageListener(0, l)
	//client.SendMessage(0, nil)
}
