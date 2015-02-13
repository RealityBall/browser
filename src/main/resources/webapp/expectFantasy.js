$(document).ready(function(){
	var charts = new Object();
	var chartsOptions = new Object();
	var loadingPositionGraphs = 0;
	var selectedDate = '';
	var charts = ['chart_position_pitcher', 'chart_position_catcher',
	'chart_position_1B', 'chart_position_2B', 'chart_position_ss', 'chart_position3B', 'chart_position_of'];
  	$.ajax({
		url: '/predictions/dates',
		cache: false
	}).done (function (predictions) {
		$('#dateList').empty();
		$.each(years, function(key, val) {
		    $('#dateList').append('<li id=\"' + val + '\"><a href=\"#\">' + val + '</a></li>');
		});
		$('ul#dateList li').on('click', handleDateSelect);
	});
	function getDataAndDrawChart(url, title, chartName) {
		$.ajax({
		  url: url,
		  dataType: 'json',
		  success: function (data, textStatus, xhr) {
			// Create our data table out of JSON data loaded from server.
			var dataTable = new google.visualization.DataTable(data);
			if (title in charts) {
				var chart = charts[title];
				chart.draw(dataTable, chartsOptions[title]);      
			} else {
				var chart 
				if (title == 'Outs') {
					var options = {
						title: title,
						isStacked: true,
						legend: { position: 'bottom' },
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.SteppedAreaChart(document.getElementById(chartName));
				} else if (title == 'Outs Types') {
					var options = {
						title: title,
						isStacked: true,
						legend: 'none',
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.PieChart(document.getElementById(chartName));
				} else if (title == 'Style') {
					var options = {
						title: title,
						isStacked: true,
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.PieChart(document.getElementById(chartName));
				} else if (title == 'Ballpark Attendance') {
					var options = {
						title: title,
						vAxis: {maxValue: 50000, minValue: 0, gridlines: {count: 6}},
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else if (title == '25 Day Ballpark Batting Average') {
					var options = {
						title: title,
						vAxis: {maxValue: 0.5, minValue: 0, gridlines: {count: 6}},
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else if (title == 'Ballpark Conditions Forecast') {
					var options = {
						title: title,
						vAxis: {maxValue: 100, minValue: 0},
						animation: {
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else if (title == 'Total Fantasy Score') {
					var options = {
						title: title,
						vAxis: {maxValue: 100, minValue: 0},
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else if (title.indexOf("Volatility") > -1) {
					var options = {
						title: title,
						vAxis: {maxValue: 0.8, minValue: 0, gridlines: {count: 5}},
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else if (title.indexOf("Moving average") > -1) {
					if ((title.indexOf("FanDuel") > -1) || (title.indexOf("Draftster") > -1)) {
						var options = {
							title: title,
							vAxis: {maxValue: 10, minValue: 0, gridlines: {count: 6}},
							animation:{
								duration: 1000,
								easing: 'out'
							}
						};
					} else {
						var options = {
							title: title,
							vAxis: {maxValue: 20, minValue: 0, gridlines: {count: 5}},
							animation:{
								duration: 1000,
								easing: 'out'
							}
						};
					}
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				} else {
					var options = {
						title: title,
						animation:{
							duration: 1000,
							easing: 'out'
						}
					};
					var chart = new google.visualization.LineChart(document.getElementById(chartName));
				}
				charts[title] = chart
				chartsOptions[title] = options
				chart.draw(dataTable, options);      
			}
			loadingPlayerGraphs--;
			if (loadingPlayerGraphs <= 0) $('#playerSelectStatus').addClass('hide');		
			loadingTeamGraphs--;
			if (loadingTeamGraphs <= 0) $('#teamSelectStatus').addClass('hide');		
		  },
		  error: function (xhr, textStatus, errorThrown) {
			console.log(textStatus);
		  }
		});
	};
	function drawPlayerChart(player, endpoint, title, chartName, gameName) {
		loadingPlayerGraphs++;
		var isBatter = $('#selectedPlayerText').text().indexOf('(P)') == -1;
		if (loadingPlayerGraphs == 1) $('#playerSelectStatus').removeClass('hide');		
		var summaryUrl = '/batter/summary?player=' + player + '&year=' + $('#selectedYearText').text();
		if (!isBatter) {
			summaryUrl = '/pitcher/summary?player=' + player + '&year=' + $('#selectedYearText').text();
		}
		$.ajax({
		  url: summaryUrl,
		  dataType: 'json',
		  cache: false,
		  success: function (data, textStatus, xhr) {
		  	var externalLinks = '';
		  	if (data.appearances.mlbId.length > 0) externalLinks = externalLinks.concat('<a href=\'http://mlb.mlb.com/team/player.jsp?player_id=' + data.appearances.mlbId + '\' target=\'_blank\'><strong>&nbsp;&nbsp;&nbsp;mlb.com</strong></a>');
		  	if (data.appearances.espnId.length > 0) externalLinks = externalLinks.concat('<a href=\'http://espn.go.com/mlb/player/_/id/' + data.appearances.espnId + '\' target=\'_blank\'><strong>&nbsp;&nbsp;&nbsp;espn.com</strong></a>');
		  	if (data.appearances.brefId.length > 0) externalLinks = externalLinks.concat('<a href=\'http://www.baseball-reference.com/players/' + data.appearances.brefId[0] + '/' + data.appearances.brefId + '.shtml\' target=\'_blank\'><strong>&nbsp;&nbsp;bref.com</strong></a>');
		  	if (isBatter) {
		  		var lineupRegime = data.appearances.lineupRegime;
		  		if (lineupRegime == 0) lineupRegime = '(UNK)'
			  	$('#playerSummary').html('<b>Bats:</b>&nbsp' + data.meta.batsWith + ',&nbsp<b>Throws:</b>&nbsp' + data.meta.throwsWith + 
			  		',&nbsp<b>RH At Bats:</b>&nbsp' + data.appearances.RHatBats + ',&nbsp<b>LH At Bats:</b>&nbsp' + data.appearances.LHatBats + 
			  		',&nbsp<b>Games:</b>&nbsp' + data.appearances.games + ',&nbsp<b>Lineup Regime:</b>&nbsp' + lineupRegime +
			  		externalLinks);
			} else {
			  	$('#playerSummary').html('<b>Bats:</b>&nbsp' + data.meta.batsWith + ',&nbsp<b>Throws:</b>&nbsp' + data.meta.throwsWith + 
			  		',&nbsp<b>Wins:</b>&nbsp' + data.appearances.wins + ',&nbsp<b>Losses:</b>&nbsp' + data.appearances.losses + 
			  		',&nbsp<b>Saves:</b>&nbsp' + data.appearances.saves + ',&nbsp<b>Avg Days Between:</b>&nbsp' + data.appearances.daysSinceLastApp +
			  		externalLinks);
			}
		  },
		  error: function (xhr, textStatus, errorThrown) {
		  	console.log(textStatus);	
		  }
		});
		var url = endpoint + '?player=' + player + '&year=' + $('#selectedYearText').text();
		if (gameName != '') {
			url = endpoint + '?player=' + player + '&gameName=' + gameName + '&year=' + $('#selectedYearText').text();
		}
		getDataAndDrawChart(url, title, chartName);
	}
	function hideChart(chartname) {
		$('#' + chartname).addClass('hide');
	}
	function showChart(chartname) {
		$('#' + chartname).removeClass('hide');
	}
	function handleDateSelect() {
		$('#selectedDateText').text(this.innerText);
		charts.map(showChart);
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/P', 'Pitcher', 'chart_position_pitcher', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/C', 'Catcher', 'chart_position_catcher', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/1B', 'First Base', 'chart_position_1B', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/2B', 'Second Base', 'chart_position_2B', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/3B', 'Third Base', 'chart_position_3B', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/SS', 'Short Stop', 'chart_position_ss', '');
		drawPlayerChart(this.id, '/predictions/' + this.innerText + '/OF', 'Outfield', 'chart_position_of', '');
	};
});
