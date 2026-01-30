math.randomseed(os.time())
local paths={"/sdcard/TouchSprite/config/cancel_attack.json","/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/cancel_attack.json"}
local logPaths={"/sdcard/TouchSprite/log/cancel_attack_config.log","/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/log/cancel_attack_config.log"}
local function writeLog(s) local t=os.date("%Y-%m-%d %H:%M:%S") for i=1,#logPaths do local f=io.open(logPaths[i],"a") if f then f:write(t.." "..s.."\n") f:close() return end end end
local function ensureDir(p) local d=p:match("(.+)/[^/]+$") if d then os.execute("mkdir -p "..d) end end
local function getStr(s,def) if s and s~="" then return s else return def end end
local function getNum(s,def) local n=tonumber(s) if n then return n else return def end end
local mode=getStr(dialogInput("检测方式(image/color)","image"),"image")
local img=getStr(dialogInput("图片名或绝对路径","auto.png"),"auto.png")
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
local function serialize(tbl) local s="return {" for k,v in pairs(tbl) do if type(v)=="string" then s=s..k.."=".."\""..v.."\"".."," else s=s..k.."="..tostring(v).."," end end return s.."}" end
local data={baseW=baseW,baseH=baseH,mode=mode,img=img,img_sim=imgSim,rect_right=rectRight,rect_left=rectLeft,rect_bottom=rectBottom,rect_top=rectTop,sample_step=stepBase,blue_ratio=blueRatio,interval_ms=interval,rounds=rounds}
for i=1,#paths do ensureDir(paths[i]) local f=io.open(paths[i],"w") if f then f:write(serialize(data)) f:close() writeLog("保存配置 "..paths[i]) return end end
writeLog("保存失败")
