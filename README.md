# http-server-java

一个由java.nio实现的轻量级http server与mvc框架

## 没计

### 为何不在request中支持文件上传

由于在解析http时要"\r\n"判定requestline, headers, body，如果通过
`channel.read(buf)`要单个字符的判断bufsize=2直到`\r\n\r\n`到
body效率低，不如直接一次read到buffer中

不能在业务中实现上传，但可以在额外实现
