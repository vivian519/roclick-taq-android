--
-- Created by IntelliJ IDEA.
-- User: huanghaojing
-- Date: 17/3/8
-- Time: 上午11:06
-- To change this template use File | Settings | File Templates.
--

local sz = {
    json = require("cjson")
}

package.loaded['szocket'] = require('socket')
package.loaded['szocket.http'] = require('socket.http')
package.loaded['szocket.url'] = require('socket.url')

local mime = require('mime')

function string:split(delimiter)
    local str = self
    if str==nil or str=='' or delimiter==nil then
        return {}
    end

    local result = {}
    for match in (str..delimiter):gmatch("(.-)"..delimiter) do
        table.insert(result, match)
    end
    return result
end

local md5 = require('luamd5/md5')
--重定义字符串方法
function string:md5()
    return md5.sumhexa(self)
end


function string:base64_decode()
    return mime.unb64(self)
end

function string:base64_encode()
    return mime.b64(self)
end

return sz