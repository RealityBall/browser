$(document).ready(function(){
	var http = location.protocol;
	var slashes = http.concat("//");
	var host = slashes.concat(window.location.host);
  	$.ajax({
		url: "/teams",
		cache: false
	}).done (function (teams) {
		$("#teamList").empty();
		$.each(teams, function(key, val) {
		    $("#teamList").append("<li id=\"" + val[0] + "\"><a href=\"#\">" + val[2] + " " + val[3] + "</a></li>");
		});
		$("ul#teamList li").on('click', handleTeamSelect);
	});
	function handleTeamSelect() {
		console.log (this.id)
		$("#selectedTeam").text(this.innerText);
		$.ajax({
			url: "/players?team=" + this.id,
			cache: false
		}).done (function (players) {
			$("#playerList").empty();
			$.each(players, function(key, val) {
				$("#playerList").append("<li><a href=\"graph?player=" + val[0] + "\">" + val[2] + " " + val[1] + " (" + val[6] + ")</li>");
			});
		});
	};
});
