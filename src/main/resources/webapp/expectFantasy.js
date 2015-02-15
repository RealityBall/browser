$(document).ready(function(){
	var charts = new Object();
	var chartsOptions = new Object();
	var loadingPositionGraphs = 0;
	var selectedDate = '';
	var selectedPlatform = '';
	var charts = ['chart_position_pitcher', 'chart_position_catcher',
	'chart_position_1b', 'chart_position_2b', 'chart_position3b', 
	'chart_position_ss', 'chart_position_of', 'chart_position_universe'];
  	$.ajax({
		url: '/predictions/dates',
		cache: false
	}).done (function (predictions) {
		$('#dateList').empty();
		$.each(predictions, function(key, val) {
		    $('#dateList').append('<li id=\"' + val + '\"><a href=\"#\">' + val + '</a></li>');
		});
		$('ul#dateList li').on('click', handleDateSelect);
		$('ul#platformList li').on('click', handlePlatformSelect);
	});
	function getDataAndDrawChart(url, title, chartName) {
		loadingPositionGraphs++;
		if (loadingPositionGraphs == 1) $('#dateSelectStatus').removeClass('hide');		
		$.ajax({
		  url: url + selectedPlatform,
		  dataType: 'json',
		  success: function (data, textStatus, xhr) {
			// Create our data table out of JSON data loaded from server.
			var dataTable = new google.visualization.DataTable(data);
			if (title in charts) {
				var chart = charts[title];
				chart.draw(dataTable, chartsOptions[title]);      
			} else {
				var options = {
					title: title,
					tooltip: { isHtml: true },
					animation:{
						duration: 1000,
						easing: 'out'
					}
				};
				var chart = new google.visualization.LineChart(document.getElementById(chartName));
				charts[title] = chart
				chartsOptions[title] = options
				chart.draw(dataTable, options);      
			}
			loadingPositionGraphs--;
			if (loadingPositionGraphs <= 0) $('#dateSelectStatus').addClass('hide');		
		  },
		  error: function (xhr, textStatus, errorThrown) {
			console.log(textStatus);
		  }
		});
	};
	function hideChart(chartname) {
		$('#' + chartname).addClass('hide');
	};
	function showChart(chartname) {
		$('#' + chartname).removeClass('hide');
	};
	function handlePlatformSelect() {
		$('#selectedPlatformText').text(this.innerText);
		selectedPlatform = this.id;
		$('#' + selectedDate).trigger('click');
	};
	function handleDateSelect() {
		$('#selectedDateText').text(this.innerText);
		selectedDate = this.id;
		charts.map(showChart);
		getDataAndDrawChart('/predictions/' + this.innerText + '/P/', 'Pitcher', 'chart_position_pitcher');
		getDataAndDrawChart('/predictions/' + this.innerText + '/C/', 'Catcher', 'chart_position_catcher');
		getDataAndDrawChart('/predictions/' + this.innerText + '/1B/', 'First Base', 'chart_position_1b');
		getDataAndDrawChart('/predictions/' + this.innerText + '/2B/', 'Second Base', 'chart_position_2b');
		getDataAndDrawChart('/predictions/' + this.innerText + '/3B/', 'Third Base', 'chart_position_3b');
		getDataAndDrawChart('/predictions/' + this.innerText + '/SS/', 'Short Stop', 'chart_position_ss');
		getDataAndDrawChart('/predictions/' + this.innerText + '/OF/', 'Outfield', 'chart_position_of');
		getDataAndDrawChart('/predictions/' + this.innerText + '/UNIVERSE/', 'Universe', 'chart_position_universe');
	};
});
