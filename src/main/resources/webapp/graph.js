$(document).ready(function(){
	var loadingGraphs = 0;
	var selectedPlayer = '';
	var selectedTeam = '';
	var batterCharts = ['chart_ba', 'chart_ba_moving', 'chart_ba_volatility', 'chart_ba_daily', 'chart_slg',
					    'chart_obp', 'chart_slg_moving', 'chart_obp_moving', 'chart_slg_volatility',
					    'chart_obp_volatility', 'chart_fanduel_fantasy', 'chart_fanduel_fantasy_moving',
					    'chart_draftkings_fantasy', 'chart_draftkings_fantasy_moving', 'chart_draftster_fantasy', 'chart_draftster_fantasy_moving'];
	var pitcherCharts = ['chart_pitcher_outs', 'chart_pitcher_style', 'chart_pitcher_strikeratio', 'chart_fanduel_fantasy', 'chart_fanduel_fantasy_moving',
					    'chart_draftkings_fantasy', 'chart_draftkings_fantasy_moving', 'chart_draftster_fantasy', 'chart_draftster_fantasy_moving'];
	var teamCharts = ['chart_team_fantasy', 'chart_ballpark_ba', 'chart_ballpark_temp', 'chart_ballpark_attendance'];
  	$.ajax({
		url: '/years',
		cache: false
	}).done (function (years) {
		$('#yearList').empty();
		$.each(years, function(key, val) {
		    $('#yearList').append('<li id=\"' + val + '\"><a href=\"#\">' + val + '</a></li>');
		});
		$('ul#yearList li').on('click', handleYearSelect);
	});
	function handleYearSelect() {
		$('#selectedYearText').text(this.innerText);
		if (selectedPlayer != '') {
			$('#' + selectedPlayer).trigger('click');
		}
		$('#playerSummary').html('');
		$.ajax({
			url: '/teams?year=' + $('#selectedYearText').text(),
			cache: false
		}).done (function (teams) {
			$('#teamList').empty();
			$.each(teams, function(key, val) {
				$('#teamList').append('<li id=\"' + val.mnemonic + '\"><a href=\"#\">' + val.city + ' ' + val.name + ' (' + val.league + ')</a></li>');
			});
			$('ul#teamList li').on('click', handleTeamSelect);
		});
	}
	function handleTeamSelect() {
		$('#selectedTeamText').text(this.innerText);
		$('#selectedPlayerText').text('Players');
		$('#playerSelectStatus').addClass('hide');
		$('#playerSummary').html('');
		selectedTeam = this.id;
		batterCharts.map(hideChart);
		pitcherCharts.map(hideChart);
		teamCharts.map(showChart);
		loadingGraphs = 0;
		var parameters = 'team=' + this.id + '&year=' + $('#selectedYearText').text();
		$.ajax({
			url: '/players?' + parameters,
			cache: false
		}).done (function (players) {
			$('ul#playerList').empty();
			var pitcherStarted = false;
			$.each(players, function(key, player) {
				if (player.position == 'P' && !pitcherStarted) {
					pitcherStarted = true;
					$('#playerList').append('<li role=\"presentation\" class=\"divider\"></li>');
				}
				$('#playerList').append('<li id=\"' + player.id + '\"><a href=\"#\">' + player.firstName + ' ' + player.lastName + ' (' + player.position + ')</a></li>');
			});
			$('ul#playerList li').on('click', handlePlayerSelect);
		});
		$.ajax({
			url: '/team/schedule?' + parameters,
			cache: false
		}).done (function (schedule) {
			$('tbody#schedule_table_body').empty();
			$.each(schedule, function(key, game) {
				var temp = '';
				if (game.temp != '0') temp = game.temp;
				var record = '';
				if (game.homeTeam == selectedTeam) record = game.record;
				$('#schedule_table_body').append('<tr>' + 
					'<td>' + game.date + '</td>' +
					'<td>' + game.visitingTeam + '</td>' +
					'<td>' + game.homeTeam + '</td>' +
					'<td>' + game.result + '</td>' +
					'<td>' + record + '</td>' +
					'<td>' + game.winningPitcher + '</td>' +
					'<td>' + game.losingPitcher + '</td>' +
					'<td>' + game.startTime + '</td>' +
					'<td>' + temp + '</td>' +
					'<td>' + game.sky + '</td>' +
					'</tr>'
					);
			});
		});
		getDataAndDrawChart('/team/fantasy?' + parameters, 'Total Fantasy Score', 'chart_team_fantasy', '');
		getDataAndDrawChart('/team/ballparkBA?' + parameters, 'Ballpark Batting Average', 'chart_ballpark_ba', '');
		getDataAndDrawChart('/team/ballparkTemp?' + parameters, 'Ballpark Temp Forecast', 'chart_ballpark_temp', '');
		getDataAndDrawChart('/team/ballparkAttendance?' + parameters, 'Ballpark Attendance', 'chart_ballpark_attendance', '');
	};
	function getDataAndDrawChart(url, title, chartName) {
		$.ajax({
		  url: url,
		  dataType: 'json',
		  success: function (data, textStatus, xhr) {
			// Create our data table out of JSON data loaded from server.
			var dataTable = new google.visualization.DataTable(data);
			var options = {
				title: title
			};
			if (title == 'Outs') {
				var options = {
					title: title,
					isStacked: true,
					legend: { position: 'bottom' }
 				};
				var chart = new google.visualization.SteppedAreaChart(document.getElementById(chartName));
				chart.draw(dataTable, options);      
			} else if (title == 'Outs Types') {
				var options = {
					title: title,
					isStacked: true,
					legend: 'none'
				};
				var chart = new google.visualization.PieChart(document.getElementById(chartName));
				chart.draw(dataTable, options);      
			} else {
				var chart = new google.visualization.LineChart(document.getElementById(chartName));
				chart.draw(dataTable, options);      
			}
			loadingGraphs--;
			if (loadingGraphs <= 0) $('#playerSelectStatus').addClass('hide');		
		  },
		  error: function (xhr, textStatus, errorThrown) {
			console.log(textStatus);
		  }
		});
	};
	function drawPlayerChart(player, endpoint, title, chartName, gameName) {
		loadingGraphs++;
		var isBatter = $('#selectedPlayerText').text().indexOf('(P)') == -1;
		if (loadingGraphs == 1) $('#playerSelectStatus').removeClass('hide');		
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
			  	$('#playerSummary').html('<b>Bats:</b> ' + data.meta.batsWith + ', <b>Throws:</b> ' + data.meta.throwsWith + ', <b>RH At Bats:</b> ' + 
			  							 data.appearances.RHatBats + ', <b>LH At Bats:</b> ' + data.appearances.LHatBats + ', <b>Games:</b> ' + data.appearances.games +
			  							 externalLinks);
			} else {
			  	$('#playerSummary').html('<b>Bats:</b> ' + data.meta.batsWith + ', <b>Throws:</b> ' + data.meta.throwsWith + ', <b>Wins:</b> ' + 
			  							 data.appearances.wins + ', <b>Losses:</b> ' + data.appearances.losses + ', <b>Saves:</b> ' + data.appearances.saves +
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
	function handlePlayerSelect() {
		$('#selectedPlayerText').text(this.innerText);
		selectedPlayer = this.id;
		if ($('#selectedPlayerText').text().indexOf('(P)') == -1) {
		  $('#playerSelectStatus').removeClass('hide');
		  batterCharts.map(showChart);
		  pitcherCharts.map(hideChart);
		  teamCharts.map(hideChart);
		  drawPlayerChart(this.id, '/batter/BA', 'As of Date Batting Average', 'chart_ba', '');
		  drawPlayerChart(this.id, '/batter/movingBA', '25 Day Batting Averages', 'chart_ba_moving', '');
		  drawPlayerChart(this.id, '/batter/volatilityBA', '100 Day Batting Average Volatility', 'chart_ba_volatility', '');
		  drawPlayerChart(this.id, '/batter/dailyBA', 'Daily Batting Average', 'chart_ba_daily', '');
		  drawPlayerChart(this.id, '/batter/slugging', 'Slugging percentage', 'chart_slg', '');
		  drawPlayerChart(this.id, '/batter/onBase', 'On base percentage', 'chart_obp', '');
		  drawPlayerChart(this.id, '/batter/sluggingMoving', '25 Day Slugging percentage', 'chart_slg_moving', '');
		  drawPlayerChart(this.id, '/batter/onBaseMoving', '25 Day On base percentage', 'chart_obp_moving', '');
		  drawPlayerChart(this.id, '/batter/sluggingVolatility', '100 Day Slugging Volatility', 'chart_slg_volatility', '');
		  drawPlayerChart(this.id, '/batter/onBaseVolatility', '100 Day On base Volatility', 'chart_obp_volatility', '');
		  drawPlayerChart(this.id, '/batter/fantasy', 'Daily FanDuel score', 'chart_fanduel_fantasy', 'FanDuel');
		  drawPlayerChart(this.id, '/batter/fantasyMoving', 'Moving average FanDuel score', 'chart_fanduel_fantasy_moving', 'FanDuel');
		  drawPlayerChart(this.id, '/batter/fantasy', 'Daily DraftKings score', 'chart_draftkings_fantasy', 'DraftKings');
		  drawPlayerChart(this.id, '/batter/fantasyMoving', 'Moving average DraftKings score', 'chart_draftkings_fantasy_moving', 'DraftKings');
		  drawPlayerChart(this.id, '/batter/fantasy', 'Daily Draftster score', 'chart_draftster_fantasy', 'Draftster');
		  drawPlayerChart(this.id, '/batter/fantasyMoving', 'Moving average Draftster score', 'chart_draftster_fantasy_moving', 'Draftster');
		} else {
		  $('#playerSelectStatus').removeClass('hide');
		  batterCharts.map(hideChart);
		  pitcherCharts.map(showChart);
		  teamCharts.map(hideChart);
		  drawPlayerChart(this.id, '/pitcher/outs', 'Outs', 'chart_pitcher_outs', '');
		  drawPlayerChart(this.id, '/pitcher/outsTypes', 'Outs Types', 'chart_pitcher_style', '');
		  drawPlayerChart(this.id, '/pitcher/strikeRatio', 'Strike Ratio', 'chart_pitcher_strikeratio', '');
		  drawPlayerChart(this.id, '/pitcher/fantasy', 'Daily FanDuel score', 'chart_fanduel_fantasy', 'FanDuel');
		  drawPlayerChart(this.id, '/pitcher/fantasyMoving', 'Moving average FanDuel score', 'chart_fanduel_fantasy_moving', 'FanDuel');
		  drawPlayerChart(this.id, '/pitcher/fantasy', 'Daily DraftKings score', 'chart_draftkings_fantasy', 'DraftKings');
		  drawPlayerChart(this.id, '/pitcher/fantasyMoving', 'Moving average DraftKings score', 'chart_draftkings_fantasy_moving', 'DraftKings');
		  drawPlayerChart(this.id, '/pitcher/fantasy', 'Daily Draftster score', 'chart_draftster_fantasy', 'Draftster');
		  drawPlayerChart(this.id, '/pitcher/fantasyMoving', 'Moving average Draftster score', 'chart_draftster_fantasy_moving', 'Draftster');
		}
	};
});
