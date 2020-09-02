function delMenu(id) {
    var fail = null;
    var url = "/menu/delMenu?id=" + id;
    $.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        async: false,
        success: function (res) {
            // console.log(res);
            // console.log(res.code == 0);
            fail = (res.code == 0);
        }
    });
    return fail;
}