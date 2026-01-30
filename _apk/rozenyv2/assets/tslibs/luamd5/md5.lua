--
-- Created by IntelliJ IDEA.
-- User: huanghaojing
-- Date: 17/3/9
-- Time: 上午11:57
-- To change this template use File | Settings | File Templates.
--


local md5_core = require("md5.core")

local md5 = {}

function md5.sumhexa(k)

    local k_md5 = md5_core.sum(k)
    return (string.gsub(k_md5, ".",
        function (c)
            return string.format("%02x", string.byte(c))
        end
        )
    )
end


return md5

