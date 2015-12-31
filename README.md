# Chukasa 

HTTP Live Streaming (HLS) Server for iOS, OS X and tvOS

[![Build Status](https://travis-ci.org/hirooka/chukasa.svg?branch=master)](https://travis-ci.org/hirooka/chukasa)

# 1. Overview

* Web camera real-time transcoding and streaming
* Video file transcoding and streaming
* PT2 / PT3 real-time transcoding and streaming
* PT2 / PT3 recording (EXPERIMENTAL)

# 2. Client

* iOS 9 Safari
* OX 11.10 Safari
* iOS App (under developing...) 
* tvOS (under developing...)

# 3. Server Installation

このソフトウェアは MIT License です．  
[LICENSE](LICENSE)

## 3.1 Docker によるお試し起動

### 動作環境
* Ubuntu 15.10
* Oracle Java 8
* Docker 1.9.1
* PT2 driver or PT3 driver (PT2/PT3 ストリーミングを行う場合)
* Web camera (Web camera ストリーミングを行う場合)

なお，コンテナを停止するとデータベースの内容はすべて消えます．  
 
サーバーのスペックとしては，Core i7 (Sandy Bridge) クラス，SSD を推奨します．  
サーバーのスペックが低すぎるとリアルタイムのトランスコードが間に合わず，正常に動作しない可能性が高いです．  

同様に Web camera のスペックもそれなりのものを推奨します．  
Web camera は Logicool C910 で動作確認しています．  
Web camera のデバイス名は /dev/video0 を前提としています．  
それ以外の場合，src/main/resources/application.properties の system.usb-camera-device-name の値を適切に変更してください．

任意のビデオファイルをトランスコードする場合，/opt/chukasa/video というディレクトリを作成し，そこにビデオファイルを配置してください．  

    sudo mkdir /opt/chukasa
    sudo chown $USER:$USER /opt/chukasa
    mkdir /opt/chukasa/video

参考までに，動作環境を構築するためのサンプル手順です．  
[local_installation_for_docker.txt](local_installation_for_docker.txt)

### GitHub から clone して設定ファイルを一部編集する．
    git clone https://github.com/hirooka/chukasa.git
    cd chukasa
    sed -i -e "s/spring.data.mongodb.host=localhost/spring.data.mongodb.host=mongo/g" src/main/resources/application.properties

### アプリケーションをビルドする．
    ./gradlew build

### アプリケーションの Docker イメージをビルドする．
    docker build -t <your_name>/chukasa:0.0.1 .

35min...

### MongoDB の Docker イメージを pull して実行する．
    docker pull mongo
    docker run --name some-mongo -d mongo

### アプリケーションの Docker イメージを実行する．
    docker run --link some-mongo:mongo --privileged --volume /dev/:/dev/ --volume /var/run/pcscd/pcscd.comm:/var/run/pcscd/pcscd.comm -v /opt/chukasa/video:/opt/chukasa/video -p 80:80 -v /etc/localtime:/etc/localtime:ro -it <your_name>/chukasa:0.0.1 /bin/bash

### アプリケーションを利用する．
iOS 9.x か OS X 10.11.x の Safari でサーバーの IP アドレスに HTTP でアクセスする．  
Start HTTP Live Streaming ボタンを押すと何かが始まる．

## 3.2 Ubuntu のサービスとして稼働

not recommended...

### 動作環境
* Ubuntu 15.10

参考までに，動作環境を構築するためのサンプル手順です．  
[local_installation_for_linux_service.txt](local_installation_for_linux_service.txt)

then...    
    
    git clone https://github.com/hirooka/chukasa.git
    cd chukasa
    ./gradlew build
    cp build/libs/chukasa-0.0.1-SNAPSHOT.jar /opt/chukasa/
    sudo ln -s /opt/chukasa/chukasa-0.0.1-SNAPSHOT.jar /etc/init.d/chukasa
    sudo update-rc.d chukasa defaults
    sudo update-rc.d chukasa enable
    sudo service chukasa start
    
iOS 9.x か OS X 10.11.x の Safari でサーバーの IP アドレスに HTTP:8080 でアクセスする．  
Start HTTP Live Streaming ボタンを押すと何かが始まる．



