function init() {
	// dat.crtTag({ "epc": "aaa", "tim": 5, tid: "ttttt" });  // 测试
	// rfid.setBank("epc");
	rfid.setBank("use");
}

// 数据处理
rfid.hdScan = function (arr) {
	var o;
	for (var i = 0; i < arr.length; i ++) {
		o = dat.ts[arr[i].tid];
		if (o) {
			o.tim += arr[i].tim;
			o.timDom.innerHTML = o.tim;
		} else {
			dat.crtTag(arr[i]);
		}
	}
};

// 连接失败的处理
rfid.hdContErr = function (arr) {
	rfid.close();
	window.location.href = "link.html";
};

dat = {
	// 标签集合
	ts: {},
	wrkb: false,	// 开关状态

	// 生成标签
	crtTag: function (o) {
		var r = {
			dom: document.createElement("div"),
			epcDom: document.createElement("div"),
			timDom: document.createElement("div"),
			delDom: document.createElement("div"),
			tim: o.tim
		};

		var d = document.createElement("div");
		d.className = "out";
		r.epcDom.className = "txt mfs";
		r.timDom.className = "tim sfs";
		r.delDom.className = "op mfs";
		d.appendChild(r.epcDom);
		d.appendChild(r.timDom);
				// // 可写入
				// var sub = d;
				// var url = "writeDemo.html?tid=";
				// url += o.tid;
				// url += "&epc=";
				// url += o.epc;
				// url += "&use=";
				// url += o.use;
				// d = document.createElement("a");
				// d.href = url;
				// d.appendChild(sub);
		r.dom.appendChild(d);
		r.dom.appendChild(r.delDom);

		r.epcDom.innerHTML = o.epc + "<br/><br/>" + o.tid + "<br/><br/>" + o.use;
		r.timDom.innerHTML = r.tim;
		r.delDom.innerHTML = "<a href=\"javascript:dat.delTag('" + o.tid + "');\">删除</a>";
		tago.appendChild(r.dom);
		dat.ts[o.tid] = r;
	},

	// 删除标签
	delTag: function (key) {
		tago.removeChild(dat.ts[key].dom);
		delete (dat.ts[key]);
	},

	// 清空集合
	clear: function () {
		tago.innerHTML = "";
		dat.ts = {};
	},

	// 开关
	wrk: function () {
		if (dat.wrkb) {
			rfid.scanStop();
			dat.wrkb = false;
			btnDom.innerHTML = "扫描";
		} else {
			rfid.scanStart();
			dat.wrkb = true;
			btnDom.innerHTML = "停止";
		}
	},

	back: function () {
		rfid.close();
	}

};
