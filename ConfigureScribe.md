# Introduction #

This is a Step-By-Step instructions of building scribed (scribe server) from sources. This instruction is very approximate and may not work on some configurations.

# Instruction #

### 1. Install pre-requisite packages ###
`Ubuntu`:
```
$ apt-get install g++ make build-essential flex bison libtool mono-gmcs libevent-dev
```
`OpenSUSE`:
```
$ zypper install g++ make flex bison libtool libevent-devel python-devel
```
### 2. Install the boost libraries (Scribe needs boost 1.36 or newer) ###
`Ubuntu`:
```
$ apt-get install libboost-all-dev
```
`OpenSUSE`:
```
$ zypper install boost-devel
```


### 3. Install thrift and fb303 ###

Get thrift source code with git, compile and install:
```
$ svn co http://svn.apache.org/repos/asf/thrift/trunk thrift
$ cd thrift
$ ./bootstrap.sh
$ ./configure
$ make
$ sudo make install
```

**Note**: On Ubuntu 10.10, thrift wan`t compile with default ruby package (update ruby version to compile or uninstall it).

Compile and install the Facebook fb303 library:
```
$ cd contrib/fb303
$ ./bootstrap.sh
$ make
$ sudo make install
```

**Note**: Maybe, there are some compilation errors in file `/thrift/contrib/fb303/cpp/gen-cpp/fb303_types.cpp` you must fix it, replace:
```
int _kfb_statusValues[] = {
  fb_status::DEAD,
  fb_status::STARTING,
  fb_status::ALIVE,
  fb_status::STOPPING,
  fb_status::STOPPED,
  fb_status::WARNING
};
```
With:
```
int _kfb_statusValues[] = {
  DEAD,
  STARTING,
  ALIVE,
  STOPPING,
  STOPPED,
  WARNING
};
```

### 4. Install Scribe ###
```
$ git clone https://github.com/facebook/scribe.git
$ cd scribe
$ ./bootstrap.sh
$ make
$ sudo make install
$ sudo ldconfig (this is necessary so that the boost shared libraries are loaded)
```

**Note**: Also maybe need to fix file `scribe/src/gen-cpp/scribe_types.cpp`:
```
int _kResultCodeValues[] = {
  ResultCode::OK,
  ResultCode::TRY_LATER
};
```
With:
```
int _kResultCodeValues[] = {
  OK,
  TRY_LATER
};
```

### 5. Initial Scribe configuration ###
  * Create configuration directory /etc/scribe
  * Copy one of the example config files from scribe/examples/example\*conf to /etc/scribe/scribe.conf -- a good one to start with is example1.conf
  * Edit /etc/scribe/scribe.conf and replace file\_path (which points to /tmp) to a location more suitable for your system
  * You may also want to replace max\_size, which dictates how big the local files can be before they're rotated (by default it's 1 MB, which is too small -- I set it to 100 MB)
  * Run scribed either with nohup or in a screen session (it doesn't seem to have a daemon mode):
```
$ scribed -c /etc/scribe/scribe.conf
```

# See also #

http://agiletesting.blogspot.com/2009/10/compiling-installing-and-test-running.html