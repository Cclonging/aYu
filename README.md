#aYu_weather
题目:天气应用
要求
1.	多界面
2.	数据存储
3.	多媒体
4.	网络
5.	共享
功能
1.	罗列中国所有的省, 市, 县 (后台接口 url: http://guolin.tech/api/china/)
2.	自动/手动定位, 展示天气信息
3.	可以查看任意城市的天气信息 (信息来源: 和风http://guolin.tech/api/weather?cityid=CN101190401&key=66e75d6f413a40c4a95e18517ec5b058    每天获取一个图片url: http://guolin.tech/api/bing_pic)
4.	可以自由切换城市, 查询天气状况
5.	可以手动更新活着后台自动更新天气状况
6.	天气状况以动画展示,首次查询某地天气, 根据不同的天气手机展示对应的背景声音 

拓扑图
 





架构
	 
 
开发
1.	技术:api25, java1.8, springboot 2.0, python3.6, Alicloud, sqlite, mysql, 开源工具(okhttp3, gson, glide)
2.	工具: Android studio ,idea, spyder
界面样式
 



