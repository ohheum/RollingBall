import socket, time, threading, sys

host = '127.0.0.1'
port = 31944

s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.settimeout(3.0)
try:
    s.connect((host,port))
except socket.error:
    print host ,'is offline, stop '
    exit()

try:
    while 0 < 100:
    	s.send(sys.stdin.readline())

except socket.error:
    print host,'is offline, stop '
    exit()
s.close()