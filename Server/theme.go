package main

import (
	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/theme"
	"image/color"
)

var serverTheme fyne.Theme = ServerTheme{}

type ServerTheme struct {
}

func (s ServerTheme) Color(name fyne.ThemeColorName, variant fyne.ThemeVariant) color.Color {
	return theme.DefaultTheme().Color(name, variant)
}

func (s ServerTheme) Font(style fyne.TextStyle) fyne.Resource {
	if style.Italic {
		if style.Bold {
			//@ts-ignore
			return resourceNotoSansCJKscBoldItalicTtf
		} else {
			//@ts-ignore
			return resourceNotoSansCJKscItalicTtf
		}
	} else {
		if style.Bold {
			//@ts-ignore
			return resourceNotoSansCJKscBoldTtf
		} else {
			//@ts-ignore
			return resourceNotoSansCJKscRegularTtf
		}
	}
}

func (s ServerTheme) Icon(name fyne.ThemeIconName) fyne.Resource {
	return theme.DefaultTheme().Icon(name)
}

func (s ServerTheme) Size(name fyne.ThemeSizeName) float32 {
	return theme.DefaultTheme().Size(name)
}
