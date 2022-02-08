# co2rm
**CoreProtect 2 ReplayMod tool**

converts block changes recorded with [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/) to a [ReplayMod](https://www.replaymod.com/) replay  
you supply an initial recording and this tool will append the specified block changes to the end of the replay

[CoreProtect GitHub](https://github.com/PlayPro/CoreProtect/) - [ReplayMod GitHub](https://github.com/ReplayMod/ReplayMod)

## demonstration

see this [demo video](https://media.discordapp.net/attachments/837350317225934910/940540021788778506/coreprotect_2_replaymod_demo.mp4) (opens in browser)

## preliminaries

* if not specified otherwise, the output file will be generated next to the input file with *.out* suffixed in before the extension.   
i.e. if your input file is *path/to/input.mcpr*, the output will be *path/to/input.out.mcpr*  
you can use the `-o <output>` argument to specify a different path or filename
* different minecraft versions use different network protocol versions. not all are implemented in this tool as packet ids/definitions change between the versions.  
currently supported versions:  
  * `1.16.4` & `1.16.5` - protocol version 754  
  * `1.18` & `1.18.1` - protocol version 757  
* minecraft block ids (like e.g. `minecraft:oak_log`) need to be converted to a numeric state id by the tool for use with the network protocol. this requires a file that provides the mapping from minecraft ids to numeric state ids (a so-called "blocks report"). these block reports differ for every minecraft version of course.  
this repository includes some block report files inside the *blocks/* directory. if you need another one, they can be generated using the `--blocks` data generator on the vanilla server, see [Data Generators](https://wiki.vg/Data_Generators)  
use the `-b <blocks_report_file>` argument to specify the blocks report file. if omitted, the tool will look for a default *blocks.json* file in the working directory.

## usage

run with `co2rm <database> <replay> [options]`   
**database** is the coreprotect sqlite database to read from  
**replay** is the baseline recording that will be extended with the blockchanges  

### Options

Required arguments:  
`-w <world>` server world name \*  
`-bb1 <x> <y> <z>` bounding box corner 1 \*  
`-bb2 <x> <y> <z>` bounding box corner 2 \*  
`-t1 <timestamp>` start time (unix timestamp) \*  
`-t2 <timestamp>` end time (unix timestamp) \*  
`-l <length>` replay length (in seconds)  

\* these arguments restrict what block changes are read form the coreprotect db. the selection is pretty straight-forward, it selects all block changes in the world *world* inside the cuboid defined by *bb1* and *bb2* and between time *t1* and *t2*.

Optional arguments:  
`-b <blocks_report_file>` specify blocks report file for namespaced id to state id mapping  
`-o <file>` recording output file to write to   
`-y` allow output overwriting  

Run with `-h` for help with all options

## building

built using [Maven](https://maven.apache.org/)

see `pom.xml` for dependencies

build with `mvn clean package`, then your dependency-packaged jar should appear in `target/co2rm.jar`

then run with `java -jar co2rm.jar <arguments>`
