math.randomseed(os.time())
local cfg={baseW=1080,baseH=2280,img_paths={"auto.png","C:/Users/zf/Documents/TSStudio/Projects/roclick-taq/roclick-taq/auto.png"},img_sim=0.85,rect_right=360,rect_left=80,rect_bottom=420,rect_top=120,sample_step=14,blue_ratio=0.30,blue={rMax=120,gMin=110,bMin=160,domGR=10,domRB=40},interval_ms=500,autorun=true,rounds=0}
local w,h=getScreenSize()
local function sx(x) return math.floor(x*w/cfg.baseW) end
local function sy(y) return math.floor(y*h/cfg.baseH) end
local jitterX,jitterY=sx(10),sy(10)
local function tap(x,y) touchDown(1,x,y) mSleep(50+math.random(0,50)) touchUp(1,x,y) mSleep(120+math.random(0,180)) end
local function attackRect() local x1=w-sx(cfg.rect_right) local x2=w-sx(cfg.rect_left) local y1=h-sy(cfg.rect_bottom) local y2=h-sy(cfg.rect_top) return {x1=x1,y1=y1,x2=x2,y2=y2} end
local function findImgInRect(path,sim,rect) local fx,fy=findImageInRegionFuzzy(path,sim or cfg.img_sim,rect.x1,rect.y1,rect.x2,rect.y2) if fx~=-1 and fy~=-1 then return fx,fy end end
local function tryImageCancel() local rect=attackRect() for _,p in ipairs(cfg.img_paths) do local fx,fy=findImgInRect(p,cfg.img_sim,rect) if fx then tap(fx+math.random(-jitterX,jitterX),fy+math.random(-jitterY,jitterY)) return true end end end
local function rgb(c) local r=math.floor(c/65536) local g=math.floor(c/256)%256 local b=c%256 return r,g,b end
local function isBlue(c) local r,g,b=rgb(c) if b>=cfg.blue.bMin and g>=cfg.blue.gMin and r<=cfg.blue.rMax and b>=g+cfg.blue.domGR and b>=r+cfg.blue.domRB then return true end end
local function tryBlueCancel() local rect=attackRect() local s=sx(cfg.sample_step) local cnt,blue=0,0 for y=rect.y1,rect.y2,s do for x=rect.x1,rect.x2,s do local c=getColor(x,y) cnt=cnt+1 if isBlue(c) then blue=blue+1 end end end local ratio=blue/cnt if ratio>=cfg.blue_ratio then local cx=math.floor((rect.x1+rect.x2)/2) local cy=math.floor((rect.y1+rect.y2)/2) tap(cx+math.random(-jitterX,jitterX),cy+math.random(-jitterY,jitterY)) return true,ratio end return false,ratio end
local function checkAndCancelAttack() if tryImageCancel() then return true end local ok=tryBlueCancel() if ok then return true end return false end
init("0",1)
if cfg.autorun then
    if cfg.rounds and cfg.rounds>0 then
        for i=1,cfg.rounds do checkAndCancelAttack() mSleep(cfg.interval_ms) end
    else
        while true do checkAndCancelAttack() mSleep(cfg.interval_ms) end
    end
end
