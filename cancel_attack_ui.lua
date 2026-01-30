math.randomseed(os.time())
toast("启动脚本，若无弹窗请开启悬浮窗权限")
mSleep(800)
local w,h=getScreenSize()
local function getStr(s,def) if s and s~="" then return s else return def end end
local function getNum(s,def) local n=tonumber(s) if n then return n else return def end end
local mode=getStr(dialogInput("检测方式(image/color)","image"),"image")
local img1=getStr(dialogInput("图片路径或文件名","auto.png"),"auto.png")
local imgSim=getNum(dialogInput("图片相似度(0.6~0.99)","0.85"),0.85)
local rectRight=getNum(dialogInput("右边距(基准像素)","360"),360)
local rectLeft=getNum(dialogInput("左边距(基准像素)","80"),80)
local rectBottom=getNum(dialogInput("下边距(基准像素)","420"),420)
local rectTop=getNum(dialogInput("上边距(基准像素)","120"),120)
local stepBase=getNum(dialogInput("采样步长(基准像素)","14"),14)
local blueRatio=getNum(dialogInput("蓝色占比阈值(0.2~0.6)","0.30"),0.30)
local interval=getNum(dialogInput("循环间隔毫秒","500"),500)
local rounds=getNum(dialogInput("循环次数(0为无限)","0"),0)
local baseW=1080
local baseH=2280
local function sx(x) return math.floor(x*w/baseW) end
local function sy(y) return math.floor(y*h/baseH) end
local jitterX,jitterY=sx(10),sy(10)
local function tap(x,y) touchDown(1,x,y) mSleep(50+math.random(0,50)) touchUp(1,x,y) mSleep(120+math.random(0,180)) end
local function attackRect() local x1=w-sx(rectRight) local x2=w-sx(rectLeft) local y1=h-sy(rectBottom) local y2=h-sy(rectTop) return {x1=x1,y1=y1,x2=x2,y2=y2} end
local _r=attackRect()
toast("设置完成，模式:"..mode.." 间隔:"..interval.."ms")
mSleep(600)
local function findImgInRect(path,sim,rect) local fx,fy=findImageInRegionFuzzy(path,sim,rect.x1,rect.y1,rect.x2,rect.y2) if fx~=-1 and fy~=-1 then return fx,fy end end
local function tryImageCancel()
    if mode~="image" then return false end
    local rect=attackRect()
    local fx,fy=findImgInRect(img1,imgSim,rect)
    if fx then toast("检测到按钮，执行取消") tap(fx+math.random(-jitterX,jitterX),fy+math.random(-jitterY,jitterY)) return true end
end
local function rgb(c) local r=math.floor(c/65536) local g=math.floor(c/256)%256 local b=c%256 return r,g,b end
local rMax,gMin,bMin,domGR,domRB=120,110,160,10,40
local function isBlue(c) local r,g,b=rgb(c) if b>=bMin and g>=gMin and r<=rMax and b>=g+domGR and b>=r+domRB then return true end end
local function tryBlueCancel() local rect=attackRect() local s=sx(stepBase) local cnt,blue=0,0 for y=rect.y1,rect.y2,s do for x=rect.x1,rect.x2,s do local c=getColor(x,y) cnt=cnt+1 if isBlue(c) then blue=blue+1 end end end local ratio=blue/cnt if ratio>=blueRatio then toast("蓝色占比"..string.format("%.2f",ratio)) local cx=math.floor((rect.x1+rect.x2)/2) local cy=math.floor((rect.y1+rect.y2)/2) tap(cx+math.random(-jitterX,jitterX),cy+math.random(-jitterY,jitterY)) return true,ratio end return false,ratio end
local function checkAndCancelAttack() if tryImageCancel() then return true end local ok=tryBlueCancel() if ok then return true end return false end
init("0",1)
if rounds>0 then
    for i=1,rounds do checkAndCancelAttack() mSleep(interval) end
    toast("检测完成:"..rounds.."次")
else
    while true do checkAndCancelAttack() mSleep(interval) end
end
