# 工程化中的Prompt技巧与多Agent、轻量微调、生产部署实践

## Prompt

大模型 prompt 指在大语言模型应用中，用于 “提示” 模型唤起特定能力以解决实际问题的提问方式。

目的：提升AI推理质量与可控性，精细化控制模型的推理质量、行为边界和输出风格。

## Prompt推理增强技术

* 思维链（Chain-of-Thought）
  
* 反思机制 (Self-Reflection)：模型自我评估与修正

## 提示模版工程

* LangChain PromptTemplate
  LangChain中提供的提示词模版语法

* Jinja2
  一种功能强大的 Python 模板引擎，可以用于提示词工程。

## Agent设计目标

目的：构建有状态、可追踪、可控的智能体，处理复杂的多轮交互任务。

### 状态管理

Agent的设计过程本质是状态机的管理。

* 对话状态机
* DST概念引入

### 上下文保持

* Memory设计模式
  * Working Memory context：工作记忆
  * Session ID + 用户图像存储
  * Long-term 保存所有历史的会话信息

### Agent对话核心技术

* Intent Recognition + Slot Filling（槽位）：理解用户意图并提取关键信息
   这一步重要，理解用户的意图才好做下一步动作
* Failure Handling：失败重试、降级策略、错误兜底

## 多Agent协作机制

多 Agent 系统（Multi-Agent System, MAS）是一种由多个自主、交互的智能体（Agent）组成的分布式系统，每个Agent具有一定的自主性、感知能力、决策能力和通信能力。

技术特点：

* 多Agent系统相比单Agent，具备天然的并行性与分布式架构，显著提升系统响应速度与资源利用率。
* 单点故障不影响整体运行，鲁棒性与容错性远超集中式单Agent系统。
* 通过协商、竞争与协作机制，可实现全局优化与自适应调整。
* 模块化设计便于系统扩展与维护，适用于大规模复杂场景。
* 在智能制造、交通、能源等工程领域，展现出更高的灵活性与可部署性。

### 多Agent的使用方式

* 协作模式：
  * Group Chat：多Agent轮流发言，达成共识（常用的方式）
  * Debate 机制：Agent间进行辩论以提升决策质量（常用的方式）
* 通信机制：
  * Message Passing：基于 send() / receive() 的异步消息机制
  * 工具调用传递：Agent A 发起调用，结果由 Agent B 处理或验证
  * 反馈循环（Feedback Loop）：Critic Agent 对输出进行评估并提出改进建议
  * 支持 human_input_mode 实现人工干预
* **高级定制与控制**：
  * Customizable ConversableAgent：深度自定义Agent行为逻辑
  * Nested Group Chat：实现“分组讨论”再汇报的复杂协作
  * Human-in-the-loop：关键节点引入人工审核与决策

### 多Agent案例 

![多Agent示例](https://raw.githubusercontent.com/wkk1994/image-repository/main/2025-08/%E5%A4%9AAgent%E7%A4%BA%E4%BE%8B.png)  

### 轻量微调方法比较

> https://zhuanlan.zhihu.com/p/1895861774558951378