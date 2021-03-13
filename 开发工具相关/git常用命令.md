## git常用命令整理

1. 追踪关系 

    * `git branch -vv` 查看当分支追踪关系<br/> 
    * `git branch --set-upstream-to origin/develop` 添加追踪关系

2. 撤销文件提交

    * 文件没有add时 使用  `git checkout -- <file>` 修改会被撤销
    * 文件add时 使用 `git reset HEAD <file>`   修改不会撤销 `git status`处于未add状态
    * 文件已经commit时 要使用版本回退 `git reset --hard HEAD`

3. 存储当前工作状态

    * `git stash` `git stash save "message"` 可以把当前工作现场“储藏”起来，等以后恢复现场后继续工作
    * `git stash list` 查看当前存储的工作
    * `git stash apply` 恢复存储的工作状态 可以恢复指定的stash `git stash apply stash@{0}`
    * `git stash pop` 恢复存储的工作状态 并 删除
    * `git stash drop` 删除存储的工作
    * `git stath show stash@{0}` 查看 存储的文件改动  `git stath show -p stash@{0}` 查看详细信息 `$ git diff stash  modules/dhc-zz-webapp/src/main/automake/bbhb/ReportFormUpdate.ui`查看单个文件改动


4. 版本回退
    
    * `git reset --soft | --mixed | --hard`
        
        + `git reset --mixed` 只是将git commit和index 信息回退到了某个版本. git reset的默认方式
        + `git reset --soft` 保留源码,只回退到commit 信息到某个版本.不涉及index的回退,如果还需要提交,直接commit即可.
        + `git reset --hard` 源码也会回退到某个版本,commit和index 都回回退到某个版本.(注意,这种方式是改变本地代码仓库源码)
    * `git reset --hard HEAD` 回退到当前版本无修改就没有变化 *HEAD*有多少个^表示回退到之前第几版 可以直接使用 *HEAD~100*
    * `git reset --haed commitId` 指定commitId回退 使用`git log`查<br/>看提交记录 `git reflog`查看提交以及回退的所有记录<br/>
    git log --graph --pretty=oneline --abbrev-commit
    * 回退之后再提交会提示错误 可以使用 `git push -f` 覆盖远程分支提交

5. 更新分支

    * `git fetch` 从远程获取最新版本到本地，不会自动merge    
      `git fetch origin master` 取回origin下master分支更新 `git fetch origin master:tmp`
      `git log -p master..origin/master` 比较本地的master分支和origin/master分支的差别
    * `git pull`  相当于是从远程获取最新版本并merge到本地

6. `git rebase` 和 `git merge`[资料](http://blog.csdn.net/hudashi/article/details/7664631/)

    * `git rebase` 用于把一个分支的修改合并到当前分支
       使用`git merge` 合并分支 分支历史看起来就像一个新的"合并的提交"
    * `git rebase origin` 这些命令会把你的"mywork"分支里的每个提交(commit)取消掉，并且把它们临时 保存为补丁(patch)<br/>
        (这些补丁放到".git/rebase"目录中),然后把"mywork"分支更新 为最新的"origin"分支，最后把保存的这些补丁应用到"mywork"分支上。
    * 解决冲突
       在rebase的过程中，也许会出现冲突(conflict). 在这种情况，Git会停止rebase并会让你去解决 冲突；在解决完冲突后，用"git-add"<br/>
        命令去更新这些内容的索引(index), 然后，你无需执行 `git-commit`,只要执行:
        `$ git rebase --continue`<br/>
        这样git会继续应用(apply)余下的补丁。在任何时候，你可以用--abort参数来终止rebase的行动，并且"mywork" 分支会回到rebase<br/>
        开始前的状态。
        `$ git rebase --abort`
    * 使用git pull命令的时候，可以使用--rebase参数，即git pull --rebase,这里表示把你的本地当前分支里的每个提交(commit)取消掉，<br/>
    并且把它们临时 保存为补丁(patch)(这些补丁放到".git/rebase"目录中),然后把本地当前分支更新 为最新的"origin"分支，最后把保存的这些<br/>
    补丁应用到本地当前分支上

7. `git merge`[资料](http://www.jianshu.com/p/58a166f24c81)

    * `git merge --no-ff` "快进式合并"（fast-farward merge）  即使可以使用fast-forward模式，也要创建一个新的合并节点
    * `git merge --squash branch_name` 将分支branch_name上的commit合并为一个commit与当前分支合并。
        git merge --squash feature-1.0.0
        git commit -m '合并提交'

8. `git revert` 

        git revert 用于反转提交,执行revert命令时要求工作树必须是干净的.git revert用一个新提交来消除一个历史提交所做的任何修改.
        revert 之后你的本地代码会回滚到指定的历史版本,这时你再 git push 既可以把线上的代码更新.(这里不会像reset造成冲突的问题)

9. git reset 和 git revert的区别

        1.reset 是直接删除指定commit，在push时会产生冲突 revert不会，revert是在正常的commit上再次提交一次commit，不过这个commit是向后提交 HEAD一直向前
        2.如果在日后现有分支和历史分支需要合并的时候,reset 恢复部分的代码依然会出现在历史分支里.但是revert 方向提交的commit 并不会出现在历史分支里.
        3.reset 是在正常的commit历史中,删除了指定的commit,这时 HEAD 是向后移动了,而 revert 是在正常的commit历史中再commit一次,只不过是反向提交,他的 HEAD 是一直向前的.

10. git push

    git push origin localbranch:remotebranch;

11. git提交时.gitignore已经忽略.iml但是不生效
   
    原因：忽略的文件以及被提交或者托管，需要执行:  
    `git rm -r --cached 文件/文件夹名字`  
    `git add .`  
    `git commit -m 'delete *.iml'`  
    清除缓存/去除托管的文件，然后提交即可。

12. git修改remote origin

    `git remote add origin url`