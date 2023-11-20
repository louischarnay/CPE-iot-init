# Program to control passerelle between Android application
# and micro-controller through USB tty
import time
import argparse
import signal
import sys
import socket
import SocketServer
import threading
import serial

HOST = "0.0.0.0"
UDP_PORT = 10000
FILENAME = "values.txt"
last_value = ""


class ThreadedUDPRequestHandler(SocketServer.BaseRequestHandler):

    def handle(self):
        data = self.request[0].strip()
        socket = self.request[1]
        current_thread = threading.current_thread()
        print("{}: client: {}, wrote: {}".format(current_thread.name,
                                                 self.client_address, data))
        global last_value

        if data != "":
            if data == "getValues()":  # Sent last value received from micro-controller
                socket.sendto(last_value, self.client_address)
            else:  # Send message through UART
                sendUARTMessage(data + ";")

            # else:
            #     print("Unknown message: ", data)


class ThreadedUDPServer(SocketServer.ThreadingMixIn, SocketServer.UDPServer):
    pass


# send serial message
SERIALPORT = "/dev/ttyACM0"
BAUDRATE = 115200
ser = serial.Serial()


def initUART():
    # ser = serial.Serial(SERIALPORT, BAUDRATE)
    ser.port = SERIALPORT
    ser.baudrate = BAUDRATE
    ser.bytesize = serial.EIGHTBITS  #number of bits per bytes
    ser.parity = serial.PARITY_NONE  #set parity check: no parity
    ser.stopbits = serial.STOPBITS_ONE  #number of stop bits
    ser.timeout = None  #block read

    # ser.timeout = 0             #non-block read
    # ser.timeout = 2              #timeout block read
    ser.xonxoff = False  #disable software flow control
    ser.rtscts = False  #disable hardware (RTS/CTS) flow control
    ser.dsrdtr = False  #disable hardware (DSR/DTR) flow control
    #ser.writeTimeout = 0     #timeout for write
    print('Starting Up Serial Monitor')
    try:
        ser.open()
    except serial.SerialException as e:
        print("Serial {} port not available".format(SERIALPORT))
        exit()


def sendUARTMessage(msg):
    try:
        ser.write(msg)
        print("Message <" + msg + "> sent to micro-controller.")
    except Exception as e:
        print(e)


def updateLastValue(value):
    global last_value
    # Remove the ';'
    last_value = value[:-1]

    # Append value to logfile
    f = open(FILENAME, "a")
    f.write(last_value + '\n')
    f.close()


# Main program logic follows:
if __name__ == '__main__':
    initUART()
    server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)
    server_thread = threading.Thread(target=server.serve_forever)
    server_thread.daemon = True

    tab = []

    try:
        server_thread.start()
        value = str()
        while ser.isOpen():
            if (ser.inWaiting() > 0):  # if incoming bytes are waiting
                data_str = ser.read(ser.inWaiting())  #Read incoming data
                value += data_str  #append data to known data
                while (';' in value):  #If delimiter is in data
                    updateLastValue(value)  #We add the whole value and log it
                    value = ""
    except (KeyboardInterrupt, SystemExit):
        ser.close()
        exit()
