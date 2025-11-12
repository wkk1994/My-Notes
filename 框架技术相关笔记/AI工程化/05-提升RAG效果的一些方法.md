# 提升RAG效果

## RAG数据加载与清洗策略

### 1.借助大模型实现数据清洗

通过大语言模型，对数据进行一些去噪化、格式标准化、内容修复等处理，可以实现数据的可靠性，提升RAG效果，一个常用的大语言模型处理数据的提升语模版:

```json
PROMPT_TEMPLATES = {
"""optimize"":"
你是一名专业的文档预处理专家,专注于为RAG知识库构建提供高质量、结构化的文本输入。请对以下文档内容进行系统性清洗与标准化处理
### 处理准则

1. **去噪净化**
  - 移除广告、页眉页脚、水印、版权说明、免责声明等非核心信息
  - 清理装饰性符号、冗余空行及特殊控制字符
  - 过滤重复或无关的页面元素(如导航栏、页码)
2. **格式标准化**
  - 统一使用UTF-8编码
  - 中文文档:采用标准中文标点(如"。"、","),避免中英混用
  - 英文部分:统一大小写规则(专有名词、缩写保留原格式)
  - 规范空格使用:去除多余空白,段落间保留单空行
  - 全角/半角符号统一(依主语言风格调整)

3. **内容修复**
  - 修正OCR常见错误(如'0'/'0'、1'/`1'/'I'、rn'/(m`)
  - 修复因换行导致的词语断裂或句子切分错误
  - 合并逻辑上连续但被分割的段落
  - 纠正明显拼写和语法错误(不改变原意前提下)
  - 对无法修复的残缺内容标注`[内容损坏]` 并保留上下文

4. **结构重构**
  - 恢复并优化文档层级结构:合理使用 #`至 ###`标题级别
  - 一级标题(`#`)用于主章节,二级(`##`)为子节,三级(`###`)为细分内容
  - 列表统一格式(有序/无序),保持缩进一致
  - 表格保持完整性,确保行列对齐、语义可读
  - 图表引用需与上下文连贯,缺失图注可补充"[图注缺失]"

5. **语义保全**
  - 不增删、不改写核心信息
  - 保留专业术语、领域关键词及技术表达
  - 维护原文逻辑关系与上下文连贯性

### 输出要求
  - **仅输出清洗后的文档内容**
  - 使用**Markdown格式**,禁止添加解释、说明或元评论
  - 不包含前缀、后缀、提示语或总结性语句
  - 为无标题的关键段落补充简洁小标题,提升结构可读性
  - 确保标题层级递进清晰,避免跳级(如`#`直接到 `###`)

请处理以下内容:

{content}
"""""
```

> 中英文符号统一的重要性：有些大模型不支持中文符号的切分，不会截断中文，。等，导致使用句子切分文本内容会不准确。

### 2.手动数据清洗

在文本加载时，实现自定义的文本切割方式，可以实现以下能力：

* 文本规范化：
  * 分词
  * 去除停用词
  * 大小写统一
  * 拼写纠错
* 编码转换与乱码处理
  * 自动检测编码（chardet）
  * 统一转为UTF-8

### 3.实体解析

• 消除实体和术语的歧义以实现一致的引用
• 使用 PyTorch 实现 BERT 实体命名识别

> 实体解析是让 RAG “理解对象是谁”的关键环节，它是从“文字匹配”到“语义匹配”的桥梁。

比如：{"苹果": "Apple_Inc", "苹果公司": "Apple_Inc", "Apple": "Apple_Inc"}都是代表苹果公司


### 4.其他策略

* 1.文档划分：合理地划分不同主题的文档，不同主题是集中在一处。
* 2.数据增强：使用同义词、释义甚至其他语言的翻译来增加语料库的多样性。
* 3.用户反馈循环：基于现实世界用户的反馈不断更新数据库，标记真实性。比如没错回答后让用户给👍/👎记录问题回答的效果。
* 4.时间敏感数据：对于经常更新的主题，实施过时机制，确保文档失效或更新。
* 5.引入外部数据集：丰富知识库信息，当数据不足的时候可以引入一些网上的标准（大家都认可）的数据来丰富知识库。

> 数据清洗会占用大量时间，很耗费人工。

## RAG的混合检索

* 语义检索（稠密检索）
* 关键词匹配（稀疏检索）

混合检索（Hybrid Search）是一种结合了语义检索（稠密检索）和关键词匹配（稀疏检索）两种优势的搜索技术。它的核心思想是：取长补短，获得更准确、更符合上下文的相关结果。

像diffy、扣子这类低代码Agent平台都是默认启用了混合检索，LlamaIndex可以通过参数进行配置。[Milvus 和 LlamaIndex下混合搜索实现](https://milvus.io/docs/zh/llamaindex_milvus_hybrid_search.md)

```python
# 安装依赖
# pip install llama-index pymilvus milvus-lite openai

from llama_index import (
    VectorStoreIndex,
    SimpleDirectoryReader,
    StorageContext,
)
from llama_index.vector_stores.milvus import MilvusVectorStore
from llama_index.embeddings.openai import OpenAIEmbedding

# 1️⃣ 初始化 Milvus 连接
milvus_vector_store = MilvusVectorStore(
    uri="http://localhost:19530",     # Milvus 实例地址
    collection_name="llamaindex_hybrid_demo",
    hybrid=True,                      # ✅ 开启混合检索
    sparse_vector_field="bm25_vector",# 稀疏向量字段名
    dense_vector_field="embedding",   # 密集向量字段名
)

# 2️⃣ 创建存储上下文
storage_context = StorageContext.from_defaults(vector_store=milvus_vector_store)

# 3️⃣ 加载文档
documents = SimpleDirectoryReader("data").load_data()

# 4️⃣ 创建索引（使用混合检索的 Milvus）
index = VectorStoreIndex.from_documents(
    documents,
    storage_context=storage_context,
    embed_model=OpenAIEmbedding(model="text-embedding-3-large"),
)

# 5️⃣ 查询引擎
query_engine = index.as_query_engine(
    similarity_top_k=5,     # 检索前5条
    hybrid_weight=0.7,      # ✅ 混合权重（0.0=纯稀疏，1.0=纯向量）
)

# 6️⃣ 查询
response = query_engine.query("介绍一下苹果公司的主要产品")
print(response)
```

**如何对语义检索（dense）与关键词检索（sparse）结果进行融合排序。**

对于这个问题常用的解决方案有如下：
| 方案 | 融合方式  | 可调参数   | 精度   | 性能   | 说明    |
| ----- | ------- | --------- | ---- | ---- | ------- |
| 线性融合      | α·dense + (1−α)·sparse | α                 | ⭐⭐   | ⭐⭐⭐⭐ | 默认最常用   |
| 归一化融合     | normalize后再加权          | α + normalization | ⭐⭐⭐  | ⭐⭐   | 精度更稳    |
| RRF排序     | 1/(k + rank) 累积        | k                 | ⭐⭐⭐  | ⭐⭐⭐  | 适合多引擎融合 |
| Re-Ranker | LLM模型重新打分              | 模型选择              | ⭐⭐⭐⭐ | ⭐    | 最高精度方案  |

最优策略 = 线性融合 + Re-Ranker 精排（这也是工业级 RAG 检索系统的标准配置）

## 知识库热更新

在RAG系统运行中，如果知识库的文件有变化（新增、删除、更新）能够及时更新到向量库中，被检索到。注意热更新也是有延迟的，只是希望降低延迟。
一般需要实现的需求是：
* 新文档上传后 → 能马上被召回
* 老文档删除后 → 不再被检索命中
* 内容修订 → 自动更新到向量数据库

LlamaIndex 通过 StorageContext + VectorStoreIndex 的分层结构支持热更新。**热更新只需操作底层 VectorStore，不需要重建 Index。**

## 知识库更新策略

上面的热更新只是增量更新，通常需要再进行一次全量更新，来合并索引碎片。实际在使用中是通过增量更新+全量更新混合使用。

* 日常小更新：使用 index.insert() 和 index.delete() 进行增量更新。
* 定期大维护：每天或每周在低峰期执行一次全量重建，以清理可能的索引碎片或元数据不一致问题。
 
增量更新：文档变动少，以新增为主，要求高可用
全量重建：文档频繁变动，对一致性要求高，可接受短暂中断

## Graph RAG

Graph RAG（Graph Retrieval-Augmented Generation）是传统RAG的增强版本，**是RAG + Knowledge Graph (KG)的融合进化版**，通过引入知识图谱结构来解决语义孤岛、上下文割裂、推理能力弱，提升信息检索与生成的质量。

* 提升检索精准度与上下文相关性
  传统RAG依赖向量相似性进行检索，容易召回语义相近但上下文无关的内容。Graph RAG 利用知识图谱中的实体关系和拓扑结构，能够实现基于语义路径的精准检索，例如通过多跳推理找到与问题间接相关的知识。
* 支持复杂推理与可解释性增强
  知识图谱天然支持逻辑推理（如路径推理、规则推理）。同时，检索路径可追溯，生成结果可附带推理链。
* 知识可维护性与系统可持续演进
  知识图谱结构化、模块化的特点，使得知识更新、纠错和扩展更加高效。

## 知识图谱的概念

知识图谱（KG） 是一种用 节点（实体） 和 边（关系） 来表示知识语义网络的结构。
通常由三元组（Triplet）构成：

```text
(实体1, 关系, 实体2)
例如：("马斯克", "创立", "特斯拉")
```

### 知识图谱生成流程

#### 1.实体识别

目的：识别出文本中的关键“名词类实体”。

实现方式：
* 传统模型：BERT + CRF / BiLSTM
* 现代做法：直接使用 LLM prompt 提取（效果更好）

LLM示例：

```text
请从以下文本中提取出所有命名实体：
实体类型包括：人物、组织、地点、时间、产品
文本：马斯克创立了特斯拉公司，并于2022年收购了推特。
LLM：
人物：马斯克
组织：特斯拉公司、推特
时间：2022年
地点：无
产品：无
```

#### 2.关系抽取
目的：判断实体之间的语义关系。
实现方式：
* 规则匹配法（基于依存句法、正则模板）
* BERT+RE 模型
* LLM Prompt 抽取法（当前主流）

LLM示例：

```text
请从以下文本中提取三元组形式的知识（实体1, 关系, 实体2）：
文本：马斯克创立了特斯拉公司，并于2022年收购了推特。
LLM：
以下是从文本中提取的三元组形式的知识：
(马斯克, 创立, 特斯拉公司)
(马斯克, 收购, 推特)
产品：无
```

#### 3.实体消歧与归一化

目的：解决“同一个实体不同写法”的问题。

常用手段：

* 语义向量匹配（Embedding）
* 基于现有知识库（如 Wikidata、CN-DBpedia）对齐
* 基于字符串相似度（Levenshtein Distance）

#### 4.构建与存储

将抽取的三元组插入到图数据库中。比如Noe4j

#### 5.图谱更新

知识更新策略：
* 定期重建（Batch）：重新从最新语料生成
* 增量更新（Incremental）：新增节点或修改属性
* 事件触发更新：基于新文档或用户反馈触发重建

LlamaIndex中GraphRAG实现代码示例：

```python
from llama_index import KnowledgeGraphIndex, SimpleDirectoryReader
from llama_index.graph_stores.neo4j import Neo4jGraphStore
from llama_index.embeddings.openai import OpenAIEmbedding

# 1️⃣ 加载文档
docs = SimpleDirectoryReader("data").load_data()

# 2️⃣ 构建图存储
graph_store = Neo4jGraphStore(username="neo4j", password="123456", url="bolt://localhost:7687")

# 3️⃣ 构建知识图谱索引
kg_index = KnowledgeGraphIndex.from_documents(
    docs,
    graph_store=graph_store,
    embed_model=OpenAIEmbedding(model="text-embedding-3-large"),
    max_triplets_per_chunk=10,
)

# 4️⃣ 查询
query_engine = kg_index.as_query_engine(include_text=True)
response = query_engine.query("宁德时代为哪些公司提供电池？")
print(response)

```

## RAG + KG 策略的常见组合

|策略类型|设计思路|流程|优&缺点|适用场景|
|--|--|--|--|--|
|级联式-先RAG再KG|先 RAG，再用 KG 做知识验证与推理增强|用户问题 → RAG 检索候选文档 → 生成候选答案 → KG 查询验证一致性 → 最终输出答案|优点：降低幻觉率、提升逻辑一致性、增强事实可解释性 <br/>缺点：依赖 KG 完备性、KG 覆盖度不足时容易误判|开放域问题、文档中存在模糊表述时。|
|级联式-先KG再RAG|先通过KG找到相关实体与关系（缩小范围），再让RAG针对这些节点的文档上下文检索详细内容。|用户提问 → 解析关键词 → 在KG中查找相关实体和路径（如找到 Apple 节点及其 CEO）→ 提取结构化信息 → 使用RAG模型将结构化数据转化为自然语言回答。|优点：减少检索范围、幻觉少 <br/>缺点：依赖 KG 完备性|知识问答、事实检索|
|并行式|同时运行 KG 检索与 RAG 检索，然后将结果融合、加权排序。|Query → [KG检索] + [RAG检索] → 结果合并 → 答案生成|优点：两种知识源互补、覆盖更广、可动态权重调节<br/>缺点：延迟略高、融合逻辑复杂（需学习排序模型）|开放问答、辅助决策|
|图增强检索|利用 KG 进行 query 扩展让语义检索更具上下文理解力。|Query → [KG检索] + [Query扩展] → RAG检索 → 答案生成|优点：召回更全、噪声低<br/>缺点：需要良好的 KG 覆盖度|企业知识搜索|
|RAG 生成增强 KG|让 RAG 结果反哺 KG，从新检索的语料中抽取实体与关系，自动更新图谱。|RAG 检索文档 → LLM提取三元组 → 更新到 KG 中|优点：动态自学习、知识持续增长<br/>缺点：需防止幻觉污染图谱（需验证机制）|动态知识库|
|混合推理|在生成阶段，LLM 同时参考：KG 提供的结构化路径；RAG 提供的语义文本；再通过 CoT（Chain-of-Thought）逻辑生成答案。|Query → KG 检索 → RAG 检索 → 合并上下文 → LLM 结构化推理生成|优点：强逻辑推理 + 可解释<br/> 缺点：计算复杂、上下文融合难|多跳问答、合规推理|

### 如何防止错误传播

RAG+KG错误传播的主要来源有两个：RAG 生成幻觉答案和KG 中存在错误/过时关系。

|方法|说明|
|--|--|
| 双向验证机制|若 RAG 输出“X 是 Y 的 CEO”，则必须在 KG 中存在(X)-[:CEO]->(Y) 才采纳；反之亦然。|
| 时效性过滤|给 KG 边添加 valid_since / valid_until 属性，避免使用过期关系（如前任CEO）|
| LLM 校验器（FactChecker）|使用 LLM 对不一致的答案进行仲裁：“根据以下信息，X是否仍是 Y 的 CEO？”|
|反馈闭环|用户反馈错误 → 记录误答案例 → 更新图谱或微调 RAG检索器|

## 评估指标

RAG的评估可以从检索阶段、生成阶段、端到端三个阶段进行划分，每个阶段都有对应的经典指标和新兴指标与开源/商用工具。

### 检索阶段

检索节点主要是衡量召回的文档是否相关、完整。

| 指标                                               | 含义                  | 常见用途         |
| ------------------------------------------------ | ------------------- | ------------ |
| **Recall@K**                                     | 前 K 个检索结果中包含正确文档的比例 | 检查召回覆盖率      |
| **Precision@K**                                  | 前 K 个结果中真正相关的比例     | 检查召回精度       |
| **MRR (Mean Reciprocal Rank)**                   | 正确文档出现位置的倒数平均       | 关注首条结果的质量    |
| **nDCG (Normalized Discounted Cumulative Gain)** | 综合考虑相关性和排序位置        | 适用于多相关文档场景   |
| **Hit Rate / Top-K Accuracy**                    | 前 K 个结果中命中目标比例      | 简单直观的检索准确性指标 |
| **Embedding Similarity**                         | 查询与结果的语义相似度         | 检查向量检索效果     |

### 生成阶段

衡量答案的正确性、流畅度和事实一致性。

| 指标                                     | 含义                  | 常见用途                 |
| -------------------------------------- | ------------------- | -------------------- |
| **ROUGE / BLEU**                       | 与参考答案的词面相似度         | 通用文本质量评估             |
| **BERTScore / Cosine Similarity**      | 嵌入层语义相似度            | 更关注语义一致性             |
| **Faithfulness / Factuality**          | 输出是否忠实于检索文档         | 检测幻觉（Hallucination）率 |
| **Context Recall / Context Precision** | 回答是否充分利用检索内容        | 评估模型是否“读懂”上下文        |
| **Answer Consistency**                 | 同一问题多次问答的一致性        | 稳定性检测                |
| **Human Evaluation**                   | 专家人工评分（正确性、流畅度、相关性） | 高质量标准验证              |


### 端到端

衡量整个 RAG pipeline 的最终表现。

| 指标                                  | 说明             |
| ----------------------------------- | -------------- |
| **End-to-End QA Accuracy**          | 模型最终回答是否正确     |
| **RAG-F1 (RAG Precision + Recall)** | 结合检索与生成质量的复合指标 |
| **Latency / Throughput**            | 系统响应时间与负载能力    |
| **Coverage**                        | 检索到知识点覆盖范围     |
| **Hallucination Rate**              | 输出中出现虚假信息的比例   |

### 常见评估工具

**RAGAS**

[RAGAS](https://docs.ragas.io/en/stable/concepts/metrics/)一个专为评估 Retrieval Augmented Generation (RAG) pipelines 而设计的开源框架，主要聚焦在前两阶段（检索阶段 + 生成阶段），评估RAG检索和生成阶段的语义一致性、上下文利用和事实忠实度。

| 层级         | 说明         | 是否被 RAGAS 评估 | 典型指标        | 说明      |
| -------------- | -------- | ------------ | --------------- | ---- |
| **🔹 检索层（Retrieval Level）**  | 衡量文档召回的相关性与充分性    | ✅ **部分覆盖**   | - Context Recall(上下文召回性)<br>- Context Precision（上下文相关性）       | RAGAS 能评估检索到的上下文是否覆盖答案、是否与问题相关（但不评估索引性能、排序算法等底层检索性能） |
| **🔹 生成层（Generation Level）** | 衡量生成内容的忠实度与正确性    | ✅ **核心覆盖**   | - Faithfulness（忠实度）<br>- Answer Relevance（答案相关性）<br>- Answer Correctness（答案正确性）         | RAGAS 的重点层，用于判断模型输出是否忠实于检索内容、是否回答了问题、是否事实正确       |

**Trulens**

TruLens 是一个端到端的RAG应用评估与监控框架，覆盖了RAG的三个阶段。

| 层级                           | 说明               | 是否覆盖 | 典型指标                                            | 示例               |
| ---------------------------- | ---------------- | ---- | ----------------------------------------------- | ---------------- |
| **🔹 检索层（Retrieval Level）**  | 评估检索结果是否相关       | ✅    | `context_relevance`、`context_precision`         | 检索到的上下文是否与问题高度相关 |
| **🔹 生成层（Generation Level）** | 评估生成答案是否正确、忠实、相关 | ✅    | `answer_relevance`、`faithfulness`、`correctness` | 回答是否忠于上下文、不胡编乱造  |
| **🔹 系统层（System Level）**     | 监控性能、延迟、成本等      | ✅    | `latency`、`cost`、`token_usage`、`feedback_score` | 对系统运行与资源消耗的端到端监控 |


> 知识图谱的生成和维护是个问题，目前没有好的生成方式，复杂关系生成可能有问题，还需要人工维护？
> 评估RAG项目的可行性，目前文档靠人工能不能查的到？
> 问的问题答案，需要从多个文档中进行组合回答：RAG + KG
> 问的问题的答案，额外需要计算统计：RAG + SQL
> 问的的问题在文档中能找到答案，但是问题的语义需要转换：RAG
> 文档重构？？推动文档所有方