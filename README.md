RollingBall
===========
client.py is Python script which is to run on Raspberry Pi.
You need to change the ip address whithin the script into that of your host pc.

After starting the RollingBall java program in your PC,
run in your Raspberry Pi as follows:

$ ./minimu9-ahrs -b /dev/e2c-1 > python client.py
