# chukasa 

Web camera, video file, PT2 and PT3 HTTP Live Streaming (HLS) Server for OS X, iOS, tvOS, Linux and Windows

[![Build Status](https://travis-ci.org/hirooka/chukasa.svg?branch=master)](https://travis-ci.org/hirooka/chukasa) [![Build Status](https://circleci.com/gh/hirooka/chukasa.png?style=shield)](https://circleci.com/gh/hirooka/chukasa)

# Overview

* Web camera real-time transcoding and streaming
* Video file transcoding and streaming
* PT2 / PT3 real-time transcoding and streaming
* PT2 / PT3 recording (EXPERIMENTAL)
* PT2 / PT3 追っかけ再生 (EXPERIMENTAL)

# Quick Start on Docker

## 1. Requirement

Mandatory

* Linux computer
* Java 8
* Docker 1.10

Option

* PT2 / PT3 environment (for PT2/PT3 streaming)
* Web camera (for web camera streaming)

## 2. Build & Run on Docker

Create direcory for application

    sudo mkdir /opt/chukasa
    sudo chown $USER:$USER /opt/chukasa
    mkdir /opt/chukasa/video

Clone project

    cd /tmp
    git clone https://github.com/hirooka/chukasa.git

Build application

    cd chukasa
    ./gradlew build

Build chukasa Docker image

    docker build -t <yourName>/chukasa:0.0.1-SNAPSHOT .


Run application on Docker

    docker run --privileged --volume /dev/:/dev/ --volume /var/run/pcscd/pcscd.comm:/var/run/pcscd/pcscd.comm -v /opt/chukasa/video:/opt/chukasa/video -p 80:80 -v /etc/localtime:/etc/localtime:ro -it <yourName>/chukasa:0.0.1-SNAPSHOT /bin/bash

## 3. Usage

When you want to do video file streaming, put video file(s) to /opt/chukasa/video.  

Access server IP address or FQDN via HTTP.

## 4. Client

Support cross-platform (OS X, iOS, tvOS, Linux and Windows)

Native

* Safari (OX 11.10, iOS 9)
* iOS App (chukasa-ios) [https://github.com/hirooka/chukasa-ios](https://github.com/hirooka/chukasa-ios)
* tvOS (play video via AirPlay)
* Microsoft Edge (Windows 10)

via MediaElement.js

* Google Chrome
* Internet Explorer
* Firefox
* Opera

# Run Anywhere

Ubuntu, Docker and AWS Elastic Beanstalk

|   | Streaming only<br>(Runnable jar) | Streaming and Recording<br>(Runnable jar + MongoDB) |
|:---:|:---:|:---:|
| Ubuntu<br>(Local) | [procedure](procedure/procedure_ubuntu_local_jar.txt) | [procedure](procedure/procedure_ubuntu_local_jar_db.txt) |
| Ubuntu<br>(Docker) | [procedure](procedure/procedure_ubuntu_local_docker_jar.txt) | [procedure](procedure/procedure_ubuntu_local_docker_jar_db.txt) |
| AWS Elastic Beanstalk<br>(Java) | [procedure](procedure/procedure_aws_elastic_beanstalk_jar.txt) | N/A |
| AWS Elastic Beanstalk<br>(Docker) | [procedure](procedure/procedure_aws_elastic_beanstalk_docker_jar.txt) | N/A |
| AWS Elastic Beanstalk<br>(Multi-container Docker) | N/A | [procedure](procedure/procedure_aws_elastic_beanstalk_multi_container_docker_jar_db.txt) |
