$(document).ready(function(){
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
		$("#selectedTeam").text(this.innerText);
		$("#selectedPlayer").text('Players');
		$.ajax({
			url: "/players?team=" + this.id,
			cache: false
		}).done (function (players) {
			$("#playerList").empty();
			$.each(players, function(key, val) {
				$("#playerList").append("<li id=\"" + val[0] + "\"><a href=\"#\">" + val[2] + " " + val[1] + " (" + val[6] + ")</a></li>");
			});
			$("ul#playerList li").on('click', handlePlayerSelect);
		});
	};
	function drawSpecificChart(player, endpoint, title, chartName) {
		var jsonData = $.ajax({
		  url: endpoint + "?player=" + player,
		  dataType:"json",
		  async: false
		}).responseText;
		// Create our data table out of JSON data loaded from server.
		var data = new google.visualization.DataTable(jsonData);
		var options = {
			title: title
		};
		var chart = new google.visualization.LineChart(document.getElementById(chartName));
		chart.draw(data, options);      
	};
	function handlePlayerSelect() {
		$("#selectedPlayer").text(this.innerText);
		drawSpecificChart(this.id, "/playerBA", "As of Date Batting Average", 'chart_ba');
		drawSpecificChart(this.id, "/playerMovingBA", "25 Day Batting Averages", 'chart_ba_moving');
		drawSpecificChart(this.id, "/playerVolatilityBA", "100 Day Batting Average Volatility", 'chart_ba_volatility');
		drawSpecificChart(this.id, "/playerDailyBA", "Daily Batting Average Volatility", 'chart_ba_daily');
		drawSpecificChart(this.id, "/playerFantasy", "Daily fantasy score", 'chart_fantasy');
		drawSpecificChart(this.id, "/playerFantasyMoving", "Moving average fantasy score", 'chart_fantasy_moving');
		drawSpecificChart(this.id, "/playerSlugging", "Slugging percentage", 'chart_slg');
		drawSpecificChart(this.id, "/playerOnBase", "On base percentage", 'chart_obp');
		drawSpecificChart(this.id, "/playerSluggingMoving", "25 Day Slugging percentage", 'chart_slg_moving');
		drawSpecificChart(this.id, "/playerOnBaseMoving", "25 Day On base percentage", 'chart_obp_moving');
		drawSpecificChart(this.id, "/playerSluggingVolatility", "100 Day Slugging Volatility", 'chart_slg_volatility');
		drawSpecificChart(this.id, "/playerOnBaseVolatility", "100 Day On base Volatility", 'chart_obp_volatility');
	};
});
