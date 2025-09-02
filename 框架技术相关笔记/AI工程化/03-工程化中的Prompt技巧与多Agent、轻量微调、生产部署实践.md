# 工程化中的Prompt技巧与多Agent、轻量微调、生产部署实践

## Prompt

大模型 Prompt 是在大语言模型应用中，通过 自然语言或结构化指令，提示和引导模型 调用其已有的知识和推理能力，以在特定上下文中 生成符合预期的输出 的输入方式。

目的：提升AI推理质量与可控性，精细化控制模型的推理质量、行为边界和输出风格。

## Prompt推理增强技术

### 思维链（Chain-of-Thought）

CoT的概念来源于一篇论文[Chain-of-Thought Prompting Elicits Reasoning in Large Language Models](https://proceedings.neurips.cc/paper_files/paper/2022/file/9d5609613524ecf4f15af0f7b31abca4-Paper-Conference.pdf)，通过生成一系列的中间推理步骤，就能够显著提高大型语言模型进行复杂推理的能力。

* Few-Shot CoT（小样本CoT提示） ：简单的在提示中提供了一些链式思考示例，足够大的语言模型的推理能力就能够被增强。简单说，就是给出一两个示例，然后在示例中写清楚推导的过程。
* Zero-Shot CoT（零样本CoT提示）：我们只需要简单地告诉模型“让我们一步步的思考”，模型就能够给出更好的答案。
* Auto-CoT（自动思维链）：不用人工写很多示例，模型自己通过提示自动生成。

  Auto-CoT 主要由两个阶段组成：
  阶段1：问题聚类：将给定问题划分为几个聚类
  阶段2：演示抽样：从每组数组中选择一个具有代表性的问题，并使用带有简单启发式的 Zero-Shot-CoT 生成其推理链

### 反思机制 (Self-Reflection)

让大模型在完成一次回答或行动后，像人类一样对自己的输出进行回顾、评估与修正，从而在下一次迭代中给出更准确、更可靠的结果。

**为什么需要反思机制**
* 大模型一次性生成的答案可能包含事实错误、逻辑漏洞或格式问题。
* 仅靠一次性提示很难让模型意识到并纠正这些错误。
* 大模型没有“真实的自信度”反馈机制

反思机制把“试错—反馈—改进”这一人类学习过程，搬到提示流程里，使模型具备自监督、自修复能力。

**核心组成（典型框架：Reflexion）**

* Actor（行动者）负责根据当前提示生成答案或执行动作。
* Evaluator（评估者）用规则或另一个 LLM 对答案打分（准确率、完整性、风格等）。
* Reflector（反思者）把“原提示 + 行动轨迹 + 评估结果”重新喂给 LLM，让它用文字总结失败原因并提出改进建议，形成一段“反思记录”。
* Memory（记忆）把反思记录追加到长期记忆，下一轮 Actor 在提示里带上这段记忆，避免再犯同类错误。

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


## 参考

* [提示工程指南](https://www.promptingguide.ai/zh)
* https://zhuanlan.zhihu.com/p/1895861774558951378