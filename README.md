# Orbis-Challenge-2016

This is my submission for the game "Cyber Team Zero" for the Orbis Challenge.
Orbis challenge is an AI hackathon where you have roughly 24 hours to write an AI to play a video game and compete agains other AIs. My AI ranked 9th out of 50 or so teams competing.

The game is played on a rectangular grid between 2 AIs. Your AI and the opponents 
AI take control of 4 units each. Each unit can move, shoot or do other 
tasks. The goal is to get more points than the opponent by the end of
the game. You can collect points in various ways such as:
- collecting pickups (weapons, heal packs)
- capturing a control points, mainframes (control points generate points, mainframes respawn your units)
- killing a enemy units

This is just the overview of the game. For more details you can check out the game manual in folder "Cyber Team Zero/ Manual"

If you want to see my AI in action, go to the "Cyber Team Zero" folder and run one of the executables called "launcher".
A window should pop up. Here you will have the option of choosing some game parameters and the AI to play the game. 
When selecting AI just choose the source code (either python or java). Then click start to view the game.

My AI is located directly in the src folder (i've included description of my strategy in the comments). I have also included AI's of other people from the competition which should be located in "src\other peoples AIs". I'd recommend playing my AI against arock's. His AI is actually stronger than mine, but his unfortunately times out if time limit is set low.
