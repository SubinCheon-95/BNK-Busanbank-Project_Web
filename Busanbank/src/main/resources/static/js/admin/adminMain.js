document.addEventListener('DOMContentLoaded', () => {
    const updateTimestamp = () => {
    const now = new Date();

    const formatTwoDigits = (number) => number.toString().padStart(2, '0');

    const year = now.getFullYear();
    const month = formatTwoDigits(now.getMonth() + 1);
    const day = formatTwoDigits(now.getDate());
    const hours = formatTwoDigits(now.getHours());
    const minutes = formatTwoDigits(now.getMinutes());

    const formattedTime = `${year}.${month}.${day} ${hours}:${minutes}`;

    const timestampElements = document.querySelectorAll('.current_timestamp');
    timestampElements.forEach(timestampElement => {
        if (timestampElement) {
            timestampElement.textContent = formattedTime;
        }
    });
};

// 페이지 로드 시 타임스탬프를 한 번 업데이트
updateTimestamp();

    // Pie Chart 생성
    const ctx = document.getElementById("product_pieChart");

    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ["외화적금", "정기예금", "환전상품", "비트코인상품", "기타"],
            datasets: [{
                backgroundColor: ["#30D97D", "#FFC271", "#FF6E79", "#BAE2EF", "#EDEEEB"],
                data: [45, 25, 15, 10, 5],
                borderWidth: 0,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                datalabels: {
                    formatter: (value, ctx) => {
                        const sum = ctx.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
                        return (value / sum * 100).toFixed(0) + "%";
                    },
                    color: '#fff',
                    font: {
                        family: 'Pretendard',
                        weight: 'bold',
                        size: 20
                    },
                        anchor: 'end',
                        align: 'start',
                        offset: 40
                },
                legend: {
                    display: true,
                    position: 'right',
                    align: 'center',
                    labels: {
                        boxWidth: 20,
                        boxHeight: 20,
                        padding: 15,
                        font: {
                            family: 'Pretendard',
                            size: 14,
                            weight: '500',
                            style: 'normal',
                            lineHeight: 1.14
                        }
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });

    //second pieChart
    const ctx2 = document.getElementById("client_pieChart");

    new Chart(ctx2, {
        type: 'pie',
        data: {
            labels: ["20대", "30대", "40대", "50대 이상"],
            datasets: [{
                backgroundColor: ["#30D97D", "#FFC271", "#FF6E79", "#BAE2EF"],
                data: [18, 37, 28, 17],
                borderWidth: 0,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                datalabels: {
                    formatter: (value, ctx) => {
                        const sum = ctx.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
                        return (value / sum * 100).toFixed(0) + "%";
                    },
                    color: '#fff',
                    font: {
                        family: 'Pretendard',
                        weight: 'bold',
                        size: 20
                    },
                        anchor: 'end',
                        align: 'start',
                        offset: 40
                },
                legend: {
                    display: true,
                    position: 'right',
                    align: 'center',
                    labels: {
                        boxWidth: 20,
                        boxHeight: 20,
                        padding: 15,
                        font: {
                            family: 'Pretendard',
                            size: 14,
                            weight: '500',
                            style: 'normal',
                            lineHeight: 1.14
                        }
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });

    const ctx3 = document.getElementById("consultation_chart");
    new Chart(ctx3, {
        type: 'bar',
        data: {
            labels: ['미답변', '처리중', '완료'],
            datasets: [{
                data: [5, 2, 18],
                backgroundColor: ['#FF6E79', '#FFC271', '#30D97D'],
                barPercentage: 0.6,
                categoryPercentage: 0.8,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                datalabels: {
                anchor: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'end' : 'center';  // 0,1은 막대 끝에, 나머지는 중앙
                },
                align: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'top' : 'center';  // 0,1은 위쪽, 나머지는 중앙
                },
                    clip: false,       // 잘림 방지
                    font: {
                        family: 'Pretendard',
                        weight: 'bold',
                        size: 13
                    },
                    formatter: (value) => value,
                    color: function(context) {
                        // 값이 0 또는 1이면 검은색, 그 외에는 흰색
                        const value = context.dataset.data[context.dataIndex];
                        return value <= 1 ? 'black' : 'white';
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 25,  // suggestedMax 대신 max 사용
                    border: {
                        display: false
                    },
                    grid: {
                        drawTicks: false,
                        drawBorder: false,
                        color: '#DDDDDD',
                        borderDash: [5, 5]
                    },
                    ticks: {
                        stepSize: 5,
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });


    const ctx4 = document.getElementById("chatBot_chart");
    new Chart(ctx4, {
        type: 'bar',
        data: {
            labels: ['미답변', '처리중', '완료'],
            datasets: [{
                data: [3, 0, 27],
                backgroundColor: ['#FF6E79', '#FFC271', '#30D97D'],
                barPercentage: 0.6,
                categoryPercentage: 0.8,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                datalabels: {
                anchor: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'end' : 'center';  // 0,1은 막대 끝에, 나머지는 중앙
                },
                align: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'top' : 'center';  // 0,1은 위쪽, 나머지는 중앙
                },
                    clip: false,       // 잘림 방지
                    font: {
                        family: 'Pretendard',
                        weight: 'bold',
                        size: 13
                    },
                    formatter: (value) => value,
                    color: function(context) {
                        // 값이 0 또는 1이면 검은색, 그 외에는 흰색
                        const value = context.dataset.data[context.dataIndex];
                        return value <= 1 ? 'black' : 'white';
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 25,  // suggestedMax 대신 max 사용
                    border: {
                        display: false
                    },
                    grid: {
                        drawTicks: false,
                        drawBorder: false,
                        color: '#DDDDDD',
                        borderDash: [5, 5]
                    },
                    ticks: {
                        stepSize: 5,
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });

    const ctx5 = document.getElementById("finance_Chart");
    new Chart(ctx5, {
        type: 'bar',
        data: {
            labels: ['미답변', '처리중', '완료'],
            datasets: [{
                data: [2, 1, 7],
                backgroundColor: ['#FF6E79', '#FFC271', '#30D97D'],
                barPercentage: 0.6,
                categoryPercentage: 0.8,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                datalabels: {
                anchor: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'end' : 'center';  // 0,1은 막대 끝에, 나머지는 중앙
                },
                align: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    return value <= 1 ? 'top' : 'center';  // 0,1은 위쪽, 나머지는 중앙
                },
                    clip: false,       // 잘림 방지
                    font: {
                        family: 'Pretendard',
                        weight: 'bold',
                        size: 13
                    },
                    formatter: (value) => value,
                    color: function(context) {
                        // 값이 0 또는 1이면 검은색, 그 외에는 흰색
                        const value = context.dataset.data[context.dataIndex];
                        return value <= 1 ? 'black' : 'white';
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 25,  // suggestedMax 대신 max 사용
                    border: {
                        display: false
                    },
                    grid: {
                        drawTicks: false,
                        drawBorder: false,
                        color: '#DDDDDD',
                        borderDash: [5, 5]
                    },
                    ticks: {
                        stepSize: 5,
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            family: 'Pretendard',
                            size: 12
                        },
                        color: '#666666'
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });
});