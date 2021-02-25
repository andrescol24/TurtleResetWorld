# TurtleResetWorld
Minecraft plugin that resets worlds

- World's backup before clears the world
- It has to safe players of suffocation: send players to spawn.
- Delete all chunks of the map except chunks claimed with GriefPrevention (with others plugins in a future)

# Backlog
1. As administrator, I need to know how long the regeneration take 
    - Load statics
2. As administrator, I need that the regeneration continues if the server crashes
    - Save the list of chunk to regen
    - Update the list of chunk to regen when a task finishes
    - when the server crashes on the init read the chunks to regen and regen only that chunks
    