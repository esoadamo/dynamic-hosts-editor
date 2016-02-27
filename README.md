# Dynamic hosts editor
As the way to connect to my computer at home from outside, I use n2n. It is great, by when I come back home a connect to home network, but hostname is still assigned to n2n IP, so I cannot use full potential of LAN. That's why created this simple program in Java, to help me use the maximum speed I can get.
# How it works
The program pings periodicly list of IP's that target hostname can gain. When ping is sucessful it will assign that IP to hostname and continues to next hostname. Order of IP's that target hostname can gain is mandatory, e.g. first specify LAN IP and then n2n IP, so if you are at home, ping on the home IP will succed and assign hostname to it, but when you are somewhere else ping to home IP will fail, so program will try to ping n2n IP, which will suceed, so hostname will be assigned to computer's n2n IP.
# Requitments
+ Java (openjdk) 7 and higher
+ Read/Write permission on hosts file
+ Read/Write permission on config file while using automatic instalation, read only otherwise

# Installation
You can download the bin/DHE.jar file, or compile it on your own, and run it. Program will automaticly show you next steps.

# Usage
After configuration is complete, program is fully automatic. Every time ping suceed, checks if IP is not already assigned, then write. If hostname is not found, adds it to the end of the file.

# Configuration
When you have your `dhe.conf` file, you can configure it this way:
> All parameters are stored as `{key}={value}`

Requied parameters are:
> `hostFile` - tells where your host file is located, on Windows is default `C:/Windows/System32/drivers/etc/hosts`, `/etc/hosts` otherwise

Optional parameters are:
> `scan` - in seconds tells how often to start pinging IPs, 180 by defaulty

> `your hostname and IPs that hostname can gain` - eg. `{home-pc}={10.0.0.54,178.56.42.11,68.51.33.21}` means first try to ping `10.0.0.54`, if succed then assign `home-pc` to `10.0.0.54`, else ping `178.56.42.11` etc. You can add as many hostnames as you want, known IPs are separated by comma.

##Sample config
> `{hostFile}={/etc/hosts}` //My hosts file location

> `{scan}={270}` //Try ping every 270 seconds

> `{my-laptop}={10.0.0.55,68.51.33.22}` //My laptop's known IPs

> `{home-pc}={10.0.0.54,178.56.42.11,68.51.33.21}` //My home PC's known IPs
