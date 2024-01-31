package soundMixer

import (
	"fmt"
	"golang.org/x/mobile/exp/audio/al"
	"io"
	"os"
)

var (
	s Sound
)

type Sound struct {
	sources   []al.Source
	buffers   []al.Buffer
	sounds    map[string]int
	initiated bool
}

func (s *Sound) Init() bool {
	if err := al.OpenDevice(); err != nil {
		fmt.Printf("ERROR: failed to open sound device: %v", err)
		return false
	}
	s.initiated = true
	s.sounds = make(map[string]int)
	return true
}

func (s *Sound) Load(name, file string, format uint32, hz int32) {
	// So we still can play w/o audio
	if !s.initiated {
		return
	}

	f, err := os.Open(file)
	defer f.Close()
	if err != nil {
		panic(err)
	}

	data, err := io.ReadAll(f)
	if err != nil {
		panic(err)
	}

	// Skip wav headers (to avoid "playing" the header)
	// We could parse this correctly, by following:
	// http://www.topherlee.com/software/pcm-tut-wavformat.html
	// But this will do for now...
	// Position 41-44 is specifying data length

	s.sources = append(s.sources, al.GenSources(1)...)
	s.buffers = append(s.buffers, al.GenBuffers(1)...)
	id := len(s.buffers) - 1
	s.sounds[name] = id

	s.buffers[id].BufferData(format, data, hz)
	s.sources[id].QueueBuffers(s.buffers[id])
}

func (s *Sound) Play(name string, volume float32) {
	if !s.initiated {
		return
	}

	id, ok := s.sounds[name]
	if !ok {
		fmt.Printf("Sound %v not found\n", name)
		return
	}

	if len(s.sources) < id {
		fmt.Printf("Error: Sound is not loaded? Len: %d Id: %d\n", len(s.sources), id)
		return
	}
	s.sources[id].SetGain(volume)
	al.PlaySources(s.sources[id])
}

func (s *Sound) Stop(name string) {
	if !s.initiated {
		return
	}

	id, ok := s.sounds[name]
	if !ok {
		fmt.Printf("Sound %v not found\n", name)
		return
	}

	if len(s.sources) < id {
		fmt.Printf("Error: Sound is not loaded? Len: %d Id: %d\n", len(s.sources), id)
		return
	}
	al.StopSources(s.sources[id])
}

func (s *Sound) Close() {
	if !s.initiated {
		return
	}
	for i := range s.sources {
		al.DeleteSources(s.sources[i])
	}

	for i := range s.buffers {
		al.DeleteBuffers(s.buffers[i])
	}
	al.CloseDevice()
}

func Init() bool {
	s = Sound{}
	return s.Init()
}
func LoadSound(name, path string) string {
	s.Load(name, path, al.FormatMono16, 44100)
	return "success"
}

func Play(name string, volume float32) string {
	s.Play(name, volume)
	return "success"
}
func Greetings(name string) bool {
	Init()
	LoadSound("wav", name)
	Play("wav", 1)
	return true
}
