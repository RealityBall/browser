$(document).ready(function(){
	var loadingGraphs = 0;
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
		$("#selectedTeamText").text(this.innerText);
		$("#selectedPlayerText").text('Players');
		$("#playerSelectStatus").addClass('hide');
		loadingGraphs = 0;
		$.ajax({
			url: "/players?team=" + this.id,
			cache: false
		}).done (function (players) {
			$("ul#playerList").empty();
			$.each(players, function(key, val) {
				$("#playerList").append("<li id=\"" + val[0] + "\"><a href=\"#\">" + val[2] + " " + val[1] + " (" + val[6] + ")</a></li>");
			});
			$("ul#playerList li").on('click', handlePlayerSelect);
		});
	};
	function drawSpecificChart(player, endpoint, title, chartName) {
		loadingGraphs++;
		if (loadingGraphs == 1) $("#playerSelectStatus").removeClass('hide');		
		$.ajax({
		  url: endpoint + "?player=" + player,
		  dataType:"json",
		  success: function (data, textStatus, xhr) {
			   // Create our data table out of JSON data loaded from server.
			   var dataTable = new google.visualization.DataTable(data);
			   var options = {
				   title: title
			   };
			   var chart = new google.visualization.LineChart(document.getElementById(chartName));
			   chart.draw(dataTable, options);      
			   loadingGraphs--;
			   if (loadingGraphs <= 0) $("#playerSelectStatus").addClass('hide');		
		  },
		  error: function (xhr, textStatus, errorThrown) {
			  console.log('a' + textStatus);
		  }
		});
	};
	function handlePlayerSelect() {
		$("#selectedPlayerText").text(this.innerText);
		drawSpecificChart(this.id, "/playerBA", "As of Date Batting Average", 'chart_ba');
		drawSpecificChart(this.id, "/playerMovingBA", "25 Day Batting Averages", 'chart_ba_moving');
		drawSpecificChart(this.id, "/playerVolatilityBA", "100 Day Batting Average Volatility", 'chart_ba_volatility');
		drawSpecificChart(this.id, "/playerDailyBA", "Daily Batting Average", 'chart_ba_daily');
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
