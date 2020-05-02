# N45 Code Editor

<p align="center">
  <img src="https://i.ibb.co/Z11hk0Q/logo.png"/>
</p>

![](https://img.shields.io/badge/build-passing%20-green)
![](https://img.shields.io/badge/built%20with-java-orange)
![](https://img.shields.io/badge/cross--platform-yes-brightgreen)
![](https://img.shields.io/badge/multi%20language-yes-yellowgreen)

N45 is a fast,simple yet elegant Code Editor. Supports all basic features of a code editor, and more. Built using Java.  Being highly user friendly, it does not take too long to get used to it. Be ready to Code away!

<p align="center">
  <img src="https://i.ibb.co/0D9ghJN/image-2.png"/>
</p>

## Features

1. **Cross Platform** - The editor is available for Mac, Windows and Linux and on every computer you own, no matter what operating system it uses.

2. **High Performance** - N45Editor is built from Swing components, providing for unmatched responsiveness.

3. **Colour Scheme Support** - Different colours are to highlight the code with adapt functions and usage.

4. **File and Folder Operations** - This editor provides the means to open, close, save, save as files to disk and to open existing files. Usually there is a menu along the top or bottom of the window.

5. **Cut,Paste,Undo,Redo** - The editor let you select or cut text and copy or move it elsewhere. Undo and Redo is also supported.

6. **Search And Replace** - It lets you search for specified strings of characters. It also allows to replace the strings when needed.

7. **Code Completion** - Small snippets and frequently appearing words in programming can be auto completed (Ctrl+Space)

8. **Inbuilt terminal** - You can run and see the output of your files without leaving the editor.

9. **Directory Tree** - A visible and selectable directory tree open in the current folder.

10. **Multible Tabs, Multiple Instances** - Multiple files can be opened in multiple tabs at the same time. Several different instances of the code editor can be operated on at the same time.

10. **Easy Installation** - A few simple commands, and you are good to go!


## Requirements

[Java](https://www.java.com/en/)

## Installation

Use git or download zip. 

```git
git clone https://github.com/nimishbongale/n45editor.git
cd n45editor
```

1. Windows

Run the following command from your CMD prompt. 

```cmd
setup
```

2. Linux

Run the following commands in your terminal.

```bash
bash setup.sh
```

If you get the following output (with no additional errors) in both cases, you should be all setup and good.

<p align="center">
  <img src="https://i.ibb.co/pXTvS25/n45.png"/>
</p>

## Usage

Now you can simply run n45edit :folderpath: on Windows. 
Example:

```
n45edit .
``` 

You could add the same to your System path to make things easier.

For Linux,

```bash 
bash n45edit.sh .
```

You could alternatively set a global alias in Linux as well

```bash
alias n45edit="bash n45edit $1"
```

Now use 

```
n45edit .
``` 

## Acknowledgements

1. [bobbylight](https://github.com/bobbylight)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[LGPL 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html)