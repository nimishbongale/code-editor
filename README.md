# N45 Code Editor

<img>
<badge>

N45 is a simple Code Editor built using 100% Java. 

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

If you get the following output in both you should be all setup and good.

<img>


## Usage

Now you can simply run n45edit <folderpath> on Windows. 
Example:

```
n45edit .
``` 

You could add the same to your System path to make things easier.

For Linux,

```bash 
bash n45edit.sh .
```

You could alternatively set a global alias in Linux

```bash
alias n45edit="bash n45edit $1"
```

Now use 

```
n45edit .
``` 

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
Please make sure to update tests as appropriate.

## License
[LGPL 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html)