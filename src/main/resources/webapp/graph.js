$(document).ready(function(){
	var loadingGraphs = 0;
	var batterCharts = ['chart_ba', 'chart_ba_moving', 'chart_ba_volatility', 'chart_ba_daily', 'chart_slg',
					    'chart_obp', 'chart_slg_moving', 'chart_obp_moving', 'chart_slg_volatility',
					    'chart_obp_volatility', 'chart_fanduel_fantasy', 'chart_fanduel_fantasy_moving',
					    'chart_draftkings_fantasy', 'chart_draftkings_fantasy_moving', 'chart_draftster_fantasy', 'chart_draftster_fantasy_moving'];
	var pitcherCharts = ['chart_pitcher_outs', 'chart_pitcher_strikeratio'];
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
		$('#selectedTeamText').text('Team');
		$('#selectedPlayerText').text('Players');
		$('#playerSummary').html('');
		$.ajax({
			url: '/teams?year=' + $('#selectedYearText').text(),
			cache: false
		}).done (function (teams) {
			$('#teamList').empty();
			$.each(teams, function(key, val) {
				$('#teamList').append('<li id=\"' + val[1] + '\"><a href=\"#\">' + val[3] + ' ' + val[4] + ' (' + val[2] + ')</a></li>');
			});
			$('ul#teamList li').on('click', handleTeamSelect);
		});
	}
	function handleTeamSelect() {
		$('#selectedTeamText').text(this.innerText);
		$('#selectedPlayerText').text('Players');
		$('#playerSelectStatus').addClass('hide');
		$('#playerSummary').html('');
		loadingGraphs = 0;
		$.ajax({
			url: '/players?team=' + this.id + '&year=' + $('#selectedYearText').text(),
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
	};
	function drawSpecificChart(player, endpoint, title, chartName, gameName) {
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
		  	if (isBatter) {
			  	$('#playerSummary').html('<b>Bats:</b> ' + data.meta.batsWith + ', <b>Throws:</b> ' + data.meta.throwsWith + ', <b>RH At Bats:</b> ' + 
			  							 data.appearances.RHatBats + ', <b>LH At Bats:</b> ' + data.appearances.LHatBats + ', <b>Games:</b> ' + data.appearances.games);
			} else {
			  	$('#playerSummary').html('<b>Bats:</b> ' + data.meta.batsWith + ', <b>Throws:</b> ' + data.meta.throwsWith + ', <b>Wins:</b> ' + 
			  							 data.appearances.wins + ', <b>Losses:</b> ' + data.appearances.losses + ', <b>Saves:</b> ' + data.appearances.saves);
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
		$.ajax({
		  url: url,
		  dataType: 'json',
		  success: function (data, textStatus, xhr) {
			// Create our data table out of JSON data loaded from server.
			var dataTable = new google.visualization.DataTable(data);
			var options = {
				title: title
			};
			var chart = new google.visualization.LineChart(document.getElementById(chartName));
			chart.draw(dataTable, options);      
			loadingGraphs--;
			if (loadingGraphs <= 0) $('#playerSelectStatus').addClass('hide');		
		  },
		  error: function (xhr, textStatus, errorThrown) {
			console.log(textStatus);
		  }
		});
	};
	function hideChart(chartname) {
		$('#' + chartname).addClass('hide');
	}
	function showChart(chartname) {
		$('#' + chartname).removeClass('hide');
	}
	function handlePlayerSelect() {
		$('#selectedPlayerText').text(this.innerText);
		if ($('#selectedPlayerText').text().indexOf('(P)') == -1) {
		  $('#playerSelectStatus').removeClass('hide');
		  batterCharts.map(showChart);
		  pitcherCharts.map(hideChart);
		  drawSpecificChart(this.id, '/batter/BA', 'As of Date Batting Average', 'chart_ba', '');
		  drawSpecificChart(this.id, '/batter/movingBA', '25 Day Batting Averages', 'chart_ba_moving', '');
		  drawSpecificChart(this.id, '/batter/volatilityBA', '100 Day Batting Average Volatility', 'chart_ba_volatility', '');
		  drawSpecificChart(this.id, '/batter/dailyBA', 'Daily Batting Average', 'chart_ba_daily', '');
		  drawSpecificChart(this.id, '/batter/slugging', 'Slugging percentage', 'chart_slg', '');
		  drawSpecificChart(this.id, '/batter/onBase', 'On base percentage', 'chart_obp', '');
		  drawSpecificChart(this.id, '/batter/sluggingMoving', '25 Day Slugging percentage', 'chart_slg_moving', '');
		  drawSpecificChart(this.id, '/batter/onBaseMoving', '25 Day On base percentage', 'chart_obp_moving', '');
		  drawSpecificChart(this.id, '/batter/sluggingVolatility', '100 Day Slugging Volatility', 'chart_slg_volatility', '');
		  drawSpecificChart(this.id, '/batter/onBaseVolatility', '100 Day On base Volatility', 'chart_obp_volatility', '');
		  drawSpecificChart(this.id, '/batter/fantasy', 'Daily FanDuel score', 'chart_fanduel_fantasy', 'FanDuel');
		  drawSpecificChart(this.id, '/batter/fantasyMoving', 'Moving average FanDuel score', 'chart_fanduel_fantasy_moving', 'FanDuel');
		  drawSpecificChart(this.id, '/batter/fantasy', 'Daily DraftKings score', 'chart_draftkings_fantasy', 'DraftKings');
		  drawSpecificChart(this.id, '/batter/fantasyMoving', 'Moving average DraftKings score', 'chart_draftkings_fantasy_moving', 'DraftKings');
		  drawSpecificChart(this.id, '/batter/fantasy', 'Daily Draftster score', 'chart_draftster_fantasy', 'Draftster');
		  drawSpecificChart(this.id, '/batter/fantasyMoving', 'Moving average Draftster score', 'chart_draftster_fantasy_moving', 'Draftster');
		} else {
		  $('#playerSelectStatus').removeClass('hide');
		  batterCharts.map(hideChart);
		  pitcherCharts.map(showChart);
		  drawSpecificChart(this.id, '/pitcher/outs', 'Outs', 'chart_pitcher_outs', '');
		  drawSpecificChart(this.id, '/pitcher/strikeRatio', 'Strike Ratio', 'chart_pitcher_strikeratio', '');
		}
	};
});
