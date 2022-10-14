$.namespace("dlog.common");

dlog.common = function () {
    var statViewOrderBy = '';
    var statViewOrderBy_old = '';
    var statViewOrderType = 'asc';
    var isOrderRequest = false;

    return {
        init: function () {
                dlog.lang.init();
        },
        resetSortMark: function () {
            var divObj = document.getElementById('th-' + statViewOrderBy);
            var old_divObj = document.getElementById('th-' + statViewOrderBy_old);
            var replaceToStr = '';
            if (old_divObj) {
                var html = old_divObj.innerHTML;
                if (statViewOrderBy_old.indexOf('[') > 0)
                    replaceToStr = '-';
                html = html.replace('▲', replaceToStr);
                html = html.replace('▼', replaceToStr);
                old_divObj.innerHTML = html
            }
            if (divObj) {
                var html = divObj.innerHTML;
                if (statViewOrderBy.indexOf('[') > 0)
                    html = '';

                if (statViewOrderType == 'asc') {
                    html += '▲';
                } else if (statViewOrderType == 'desc') {
                    html += '▼';
                }
                divObj.innerHTML = html;
            }
            isOrderRequest = true;

            this.ajaxRequestForBasicInfo();
            return false;
        },

        setOrderBy: function (orderBy) {
            if (statViewOrderBy != orderBy) {
                statViewOrderBy_old = statViewOrderBy;
                statViewOrderBy = orderBy;
                statViewOrderType = 'desc';
                dlog.common.resetSortMark();
                return;
            }

            statViewOrderBy_old = statViewOrderBy;

            if (statViewOrderType == 'asc')
                statViewOrderType = 'desc'
            else
                statViewOrderType = 'asc';

            dlog.common.resetSortMark();
        },

        ajaxuri: "",
        handleCallback: null,
        handleAjaxResult: function (data) {
            dlog.common.handleCallback(data);
            if (!isOrderRequest) {
                dlog.lang.trigger();
            }
        },//ajax 处理函数
        ajaxRequestForBasicInfo: function (data) {
            $.ajax({
                type: 'get',
                // url: dlog.common.getAjaxUrl(dlog.common.ajaxuri),
                data: data,
                url: dlog.common.ajaxuri,
                success: function (data) {
                    dlog.common.handleAjaxResult(data);
                },
                dataType: "json"
            });
        },
        subLogString: function (sql, len) {
            if (sql == undefined || sql == null) {
                return '';
            }

            if (sql.length <= len)
                return sql;
            return sql.substr(0, len) + '...';
        },
        stripes: function () {
            $("#dataTable tbody tr").each(function () {
                $(this).removeClass("striped");
            });
            $("#dataTable tbody tr:even").each(function () {
                $(this).addClass("striped");
            });
        },

        getUrlVar: function (name) {
            var vars = {};
            var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
                vars[key] = value;
            });
            return vars[name];
        }
    }
}();

$(document).ready(function () {
    dlog.common.init();
});

function replace(data) {
    if ((!data) || data === undefined) {
        return '';
    } else {
        return format(data);
    }
}

function format(s) {
    var str = s += '';
    return str.replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
}
