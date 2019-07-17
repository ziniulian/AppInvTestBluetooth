
rfid.bleScanEnd = function (dev) {
	dat.clearNan();
	btnDom.className = "bar";
	// TODO : 设备列表信息补充
};
rfid.bleScanDev = function (dev) {
	if (dev.typ === 2 && dev.rsi < -20) {
		dat.clearNan();
		var d = document.createElement("br");
		devsDom.appendChild(d);
		d = document.createElement("br");
		devsDom.appendChild(d);
		d = document.createElement("br");
		devsDom.appendChild(d);
		d = document.createElement("a");
		d.href = "javascript: dat.link(\"" + dev.adr + "\");";
		d.innerHTML = dev.adr;
		if (dev.nam) {
			d.innerHTML += " (" + dev.nam + ")";
		}
		devsDom.appendChild(d);
	}
};

var dat = {
	nan: false,
	init: function () {
		var adr = rfid.getDev();
		if (adr) {
			var d = document.createElement("br");
			devsDom.appendChild(d);
			d = document.createElement("br");
			devsDom.appendChild(d);
			d = document.createElement("br");
			devsDom.appendChild(d);
			d = document.createElement("br");
			devsDom.appendChild(d);
			d = document.createElement("br");
			devsDom.appendChild(d);
			d = document.createElement("a");
			d.href = "linking.html";
			d.innerHTML = "重连";
			devsDom.appendChild(d);
		} else {
			dat.scan();
		}
	},
	scan: function () {
		btnDom.className = "Lc_nosee";
		devsDom.innerHTML = "<br/><br/><br/><br/><br/>刷新中。。。";
		dat.nan = true;
		rfid.bleScan();
	},
	link: function (adr) {
		rfid.setDev(adr);
		window.location.href = "linking.html";
	},
	clearNan: function () {
		if (dat.nan) {
			devsDom.innerHTML = "";
			dat.nan = false;
		}
	}
};
