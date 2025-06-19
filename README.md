# 项目简介
# 技术栈说明
# 安装和运行说明
# 贡献指南
# 开源协议说明
- 遵循《Apache License》Version 2.0。
- 有关必要声明和归属信息，请查看 NOTICE.txt 文件。



# 运行 Checkstyle 检查
./gradlew checkstyleMain checkstyleTest

# 运行 SpotBugs 检查
./gradlew spotbugsMain spotbugsTest

# 运行所有检查
./gradlew check




# JWT_SECRET 生成
推荐生成512位
openssl rand -base64 64


~~~bash
# 生成生产环境secret
openssl rand -base64 64 > /secure/location/jwt.secret
# 设置环境变量
export JWT_SECRET=$(cat /secure/location/jwt.secret)
~~~
