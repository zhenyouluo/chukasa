var Template = function() { return `<?xml version="1.0" encoding="UTF-8" ?>

<document>

<catalogTemplate>

<banner>

<title>Chukasa for tvOS</title>

</banner>

<list>

<section>

<listItemLockup>

<title>Channels</title>

<decorationLabel>10</decorationLabel>

<relatedContent>

<grid>

<section>

<lockup videoURL="${this.BASEURL}/player/stop">
<img src="${this.BASEURL}/images/stop.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/player/remove">
<img src="${this.BASEURL}/images/remove.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/stream/live/5000/chukasa.m3u8">
<img src="${this.BASEURL}/images/playlist.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/player/start?streamingtype=WEB_CAMERA&amp;ch=20&amp;videobitrate=5000&amp;duration=300&amp;encrypted=true">
<img src="${this.BASEURL}/images/20.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/player/start?streamingtype=CAPTURE&amp;ch=20&amp;videobitrate=5000&amp;duration=0&amp;encrypted=true">
<img src="${this.BASEURL}/images/20.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/player/start?streamingtype=CAPTURE&amp;ch=23&amp;videobitrate=5000&amp;duration=0&amp;encrypted=true">
<img src="${this.BASEURL}/images/23.png" width="480" height="320" />
</lockup>

<lockup videoURL="${this.BASEURL}/player/start?streamingtype=CAPTURE&amp;ch=22&amp;videobitrate=5000&amp;duration=0&amp;encrypted=true">
<img src="${this.BASEURL}/images/22.png" width="480" height="320" />
</lockup>

</section>

</grid>

</relatedContent>

</listItemLockup>

</section>

</list>

</catalogTemplate>

</document>`

}