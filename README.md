# mecm-appo

＃＃＃＃ 描述
应用编排器是负责编排应用生命周期维护操作的核心模块。

####编译和构建
Appo项目基于docker容器化，在编译和构建过程中分为两个步骤。

####编译
Appo是一个基于jdk1.8和maven编写的Java程序。 编译只需执行 mvn install 即可编译生成jar包

#### 构建镜像
Appo 项目提供了一个用于镜像的 dockerfile 文件。 制作镜像时可以使用以下命令

docker build -t edgegallery/mecm-appo:latest -f docker/Dockerfile 。