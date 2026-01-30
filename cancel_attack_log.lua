math.randomseed(os.time())
local cfg={baseW=1080,baseH=2280,mode="image",img="auto.png",img_sim=0.85,rect_right=360,rect_left=80,rect_bottom=420,rect_top=120,sample_step=14,blue_ratio=0.30,interval_ms=500,rounds=0,log_name="cancel_attack_log"}
local cfgPaths={"/sdcard/TouchSprite/config/cancel_attack.json","/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/cancel_attack.json"}
local cfgText=nil
local cfgPath=nil
for i=1,#cfgPaths do
    local f=io.open(cfgPaths[i],"r")
    if f then cfgText=f:read("*a") f:close() cfgPath=cfgPaths[i] break end
end
init("0",1)
mSleep(150)
local w,h=getScreenSize()
if w<=0 or h<=0 then mSleep(200) w,h=getScreenSize() end
local function sx(x) return math.floor(x*w/cfg.baseW) end
local function sy(y) return math.floor(y*h/cfg.baseH) end
local jitterX,jitterY=sx(10),sy(10)
local function tap(x,y) touchDown(1,x,y) mSleep(50+math.random(0,50)) touchUp(1,x,y) mSleep(120+math.random(0,180)) end
local function attackRect() local x1=w-sx(cfg.rect_right) local x2=w-sx(cfg.rect_left) local y1=h-sy(cfg.rect_bottom) local y2=h-sy(cfg.rect_top) return {x1=x1,y1=y1,x2=x2,y2=y2} end
local function findImgInRect(path,sim,rect) local fx,fy=findImageInRegionFuzzy(path,sim or cfg.img_sim,rect.x1,rect.y1,rect.x2,rect.y2) if fx~=-1 and fy~=-1 then return fx,fy end end
local function log(s)
    local t=os.date("%Y-%m-%d %H:%M:%S")
    local line=t.." "..s
    local paths={"/sdcard/TouchSprite/log/cancel_attack.log","/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/log/cancel_attack.log"}
    for i=1,#paths do
        local f=io.open(paths[i],"a")
        if f then f:write(line.."\n") f:flush() f:close() return true end
    end
end
local function loadCfg(text)
    if not text or #text==0 then return nil,"empty" end
    local chunk,err
    if string.match(text,"^%s*return") then
        chunk,err=load(text)
    else
        chunk,err=load("return "..text)
    end
    if not chunk then return nil,err end
    local ok,t=pcall(chunk)
    if ok and type(t)=="table" then return t end
    return nil,"not_table"
end
do
    local t,err=loadCfg(cfgText)
    if t then
        for k,v in pairs(t) do cfg[k]=v end
        log("加载配置 "..cfgPath)
    else
        if err=="empty" then log("未发现配置文件") else log("配置解析失败 "..tostring(err)) end
    end
end
local function tryImageCancel()
    if cfg.mode~="image" then return false end
    local rect=attackRect()
    local fx,fy=findImgInRect(cfg.img,cfg.img_sim,rect)
    if fx then
        log("图片命中("..fx..","..fy..") 执行取消")
        tap(fx+math.random(-jitterX,jitterX),fy+math.random(-jitterY,jitterY))
        return true
    else
        log("图片未命中")
    end
end
local function rgb(c) local r=math.floor(c/65536) local g=math.floor(c/256)%256 local b=c%256 return r,g,b end
local function isBlue(c) local r,g,b=rgb(c) if b>=160 and g>=110 and r<=120 and b>=g+10 and b>=r+40 then return true end end
local function tryBlueCancel()
    local rect=attackRect()
    local s=sx(cfg.sample_step)
    local cnt,blue=0,0
    for y=rect.y1,rect.y2,s do
        for x=rect.x1,rect.x2,s do
            local c=getColor(x,y)
            cnt=cnt+1
            if isBlue(c) then blue=blue+1 end
        end
    end
    local ratio=blue/cnt
    log("蓝色占比"..string.format("%.2f",ratio))
    if ratio>=cfg.blue_ratio then
        local cx=math.floor((rect.x1+rect.x2)/2)
        local cy=math.floor((rect.y1+rect.y2)/2)
        log("占比满足阈值 执行取消")
        tap(cx+math.random(-jitterX,jitterX),cy+math.random(-jitterY,jitterY))
        return true,ratio
    end
    return false,ratio
end
local function checkAndCancelAttack() if tryImageCancel() then return true end local ok=tryBlueCancel() if ok then return true end return false end
init("0",1)
log("启动 分辨率 "..w.."x"..h.." 模式 "..cfg.mode.." 间隔 "..cfg.interval_ms.."ms")
if cfg.rounds and cfg.rounds>0 then
    for i=1,cfg.rounds do checkAndCancelAttack() mSleep(cfg.interval_ms) end
    log("完成 循环 "..cfg.rounds)
else
    while true do checkAndCancelAttack() mSleep(cfg.interval_ms) end
end
