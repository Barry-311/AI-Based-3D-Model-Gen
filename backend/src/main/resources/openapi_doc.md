# OpenAPI definition


**简介**:OpenAPI definition


**HOST**:http://localhost:8123/api


**联系人**:


**Version**:v0


**接口路径**:/api/v3/api-docs/default


[TOC]






# app-controller


## uploadPicture


**接口地址**:`/api/app/upload`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## createGenerationTask


**接口地址**:`/api/app/generate`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ModelGenerateResponse|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||ResponseData|ResponseData|
|&emsp;&emsp;task_id||string||
|message||string||
|taskId||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"task_id": ""
	},
	"message": "",
	"taskId": ""
}
```


## generateModelWithProgress


**接口地址**:`/api/app/generate-stream`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`text/event-stream`


**接口描述**:


**请求示例**:


```javascript
{
  "prompt": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|modelGenerateStreamRequest|ModelGenerateStreamRequest|body|true|ModelGenerateStreamRequest|ModelGenerateStreamRequest|
|&emsp;&emsp;prompt|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ServerSentEventModel3DVO|


**响应参数**:


暂无


**响应示例**:
```javascript
[
	null
]
```


## generateAugmentedModelWithProgress


**接口地址**:`/api/app/generate-stream-augmented`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`text/event-stream`


**接口描述**:


**请求示例**:


```javascript
{
  "prompt": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|modelGenerateStreamRequest|ModelGenerateStreamRequest|body|true|ModelGenerateStreamRequest|ModelGenerateStreamRequest|
|&emsp;&emsp;prompt|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ServerSentEventModel3DVO|


**响应参数**:


暂无


**响应示例**:
```javascript
[
	null
]
```


## getTaskStatus


**接口地址**:`/api/app/status/{taskId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|taskId||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|TaskStatusResponse|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||TaskData|TaskData|
|&emsp;&emsp;type||string||
|&emsp;&emsp;status||string||
|&emsp;&emsp;progress||integer(int32)||
|&emsp;&emsp;input||object||
|&emsp;&emsp;output||Output|Output|
|&emsp;&emsp;&emsp;&emsp;model||string||
|&emsp;&emsp;&emsp;&emsp;base_model||string||
|&emsp;&emsp;&emsp;&emsp;pbr_model||string||
|&emsp;&emsp;&emsp;&emsp;rendered_image||string||
|&emsp;&emsp;task_id||string||
|&emsp;&emsp;create_time||integer(int64)||
|message||string||
|output||Output|Output|
|&emsp;&emsp;model||string||
|&emsp;&emsp;base_model||string||
|&emsp;&emsp;pbr_model||string||
|&emsp;&emsp;rendered_image||string||
|status||string||
|progress||integer(int32)|integer(int32)|


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"type": "",
		"status": "",
		"progress": 0,
		"input": {},
		"output": {
			"model": "",
			"base_model": "",
			"pbr_model": "",
			"rendered_image": ""
		},
		"task_id": "",
		"create_time": 0
	},
	"message": "",
	"output": {
		"model": "",
		"base_model": "",
		"pbr_model": "",
		"rendered_image": ""
	},
	"status": "",
	"progress": 0
}
```


## downloadObject


**接口地址**:`/api/app/download/{appId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||


**响应参数**:


暂无


**响应示例**:
```javascript

```


## augmentPrompt


**接口地址**:`/api/app/augment/prompt`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`text/event-stream`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||query|true|integer(int64)||
|message||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ServerSentEventString|


**响应参数**:


暂无


**响应示例**:
```javascript
[
	null
]
```


# health-controller


## healthCheck


**接口地址**:`/api/health/`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseString|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||string||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": "",
	"message": ""
}
```