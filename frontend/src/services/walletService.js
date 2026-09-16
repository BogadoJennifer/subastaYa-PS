function getAuctionById(auctionId) {
  return axios.get(`${this.baseUrl}/auctions/${auctionId}`);
}