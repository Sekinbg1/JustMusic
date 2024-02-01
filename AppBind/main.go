package main

import (
	"encoding/json"
	"fmt"
)

type test1 struct {
	Name  string `json:"name"`
	Name1 string `json:"name1"`
}
type test2 struct {
	Name map[string]test1
}

func main() {
	a := test2{Name: map[string]test1{"a": {Name: "test"}}}
	d, _ := json.Marshal(a)
	fmt.Println(string(d))
}
