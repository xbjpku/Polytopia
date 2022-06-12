# Polytopia
Java Term Project

### /resources
Currently contains textures for the game. The /resources/textures.XML can be modified to include new textures. Refer to `Texture.java` for how the key-value pair should be formatted.

### /polytopia
Source code for the game. It consists of 4 packages:

#### gameplay

Implements core game logic, and the Action-Consequence framework on which the game AI, graphics and GUI can be built. For debugging and demonstration, the `Tile.java` also has a makeshift `main` method, which generates a map, provides some basic graphics and camera work, plus a command line interface for interacting with the game before GUI is tackled.

* `Action.java`: Implements all Actions in the game. Actions are what players can do in one step, both for the human player and the bots. GUI, graphics and AI should only care about the abstract methods declared in the `Action` interface.

* `Consequence.java`: Implements all Consequences in the game, which are caused by Actions. Basically, Actions depict **cost**, while Consequences depict **reward** (can be negative, though). The involved game logic is hidden under the iteration of actions and  consequences, while from the outside they can be treated equally.

* `Game.java`: The game instance. Meant to be a static global class storing game-level information, like the game map and current player. Not finished yet.

* `Player.java`: Describes a player instance, both for human players and bots. Encapsulates per-player states, and interacts with Action and Consequence, implementing game logic. Also declares Faction and Tech as nested class.

* `Tile.java`: Describes a tile on the game map.

* `TileVariation.java`: Describes the variation that can be attached to a tile, including Resource, Improvement and City. 

* `Unit.java`: Describes a unit in the game. 

* `AI.java`: Implements game bot that players play against.

* `TileMap.java`: Describes a map in the game, and implements the map generation. Also contains some useful utility methods for working with tiles on map.

#### graphics

Implements graphics for the game. 

* `Texture.java`: Contains a hashmap as a pool of textures, which the temporary rendering code in `Tile.java` uses to draw the map. The getter methods can be modified, if the hash format were to be changed, or if more detailed texturing is wanted.

* `Visualizable.java`: Contains only an interface for now, indicating that the class implementing this should be tackled by graphics and GUI. Should at least be modified, so that the `visualize()` method has appropriate GUI/graphics related arguments to do the work.

* `Camera.java`: Implements camera for rendering.

* `Render.java`: The actual render engine.

* `Motion.java` and `Movable.java`: Implements animation that can be rendered by `Render.java`.

#### utils

All the auxiliary helper functions.

* `RandomName.java`: A static pool of names, for naming cities in the game.

* `SimplexNoise.java`: Generation of the simplex noise, currently used in map generation.

* `XML.java`: Help create a formatted XML file, so that we can modify it as we need. Should be of not much use now.

* `SoundAdapter.java`: Out of use.

* `MySliderUI.java`: Out of use.

* `CircleButton.java`: Implements the buttons in the game window.


#### window

High-level GUI.

* `GameWindow.java`: The game window itself, with GUI layout and interactive logic.

* `LaunchWindow.java`: The launch window. Load game is not yet complete.

* `MainPanel.java`: The background image.

* `OptionWindow.java`: Out of use.


### Compiling and Running (for testing)

In terminal, `cd` to the root directory (which contains this `readme`, `resources` and `polytopia`).
Issue
```
javac -cp . polytopia/gameplay/XXX.java -d bin  
```
To compile `XXX.java` (and all related source code files), storing the class files to `/bin`. The `/bin` directory is ignored by `git`, and should not be manually operated as well.
Then, to run a class with `main` method, issue
```
java -cp bin packageName.className
```
For example, to run the main method in `LaunchWindow.java`, type in
```
java -cp bin polytopia.window.LaunchWindow 
```

### Playing the Game
Just use
```
java -jar Polytopia.jar
```
to run the packed game. Directly clicking on this file would not work, as the resources are loaded according to relative paths. For now, you can only run this file in terminal.

