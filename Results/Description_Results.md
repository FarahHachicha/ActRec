**Results**
This folder contains the outcomes and visualizations of our action recommendation approach, __ActRec__ (short for Action Recommendation). Additionally, it includes results from the individual `Collaborative Filtering approach (CF)` and the `Association Rule Mining approach (ARM)` for comparison purposes in terms of Precision, Recall, and Success Rate.

``Boxplot_Precision``: These files contain boxplots illustrating the precision of the recommendation for 10 actions, while varying the value of K, which represents the number of actions to recommend. The boxplots reveal that as the value of K increases, the precision of the recommendations decreases.

``Boxplot_Recall``: In these files, you will find boxplots depicting the recall of the recommendation for 10 actions as the value of K changes. Recall increases with an increase in K, representing the number of actions recommended.

``PRC (Precision-Recall Curve)``: These files present the Precision-Recall Curve, showcasing the relationship between precision, recall, and the number of actions to recommend (K). As K increases, recall rises while precision decreases. Each point on the curve corresponds to a specific value of K (i.e., K = i), indicating the average recall on the x-axis and the average precision on the y-axis when recommending i actions.

``SR (Success Rate)``: The Success Rate files display the average success rate for different values of K. As K increases, the success rate also improves, demonstrating the effectiveness of the recommendations for larger numbers of actions.

These visualizations and results provide valuable insights into the performance and behavior of our "ActRec" approach, as well as comparisons with the individual CF and ARM approaches. The findings showcase the strengths of our combined approach and its ability to generate accurate and useful recommendations for GitHub projects utilizing GitHub Actions.