// SPDX-License-Identifier: MIT

pragma solidity 0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Pausable.sol";
import "./AbstractSusTokenRequester.sol";
import "./StructData.sol";

contract SusToken is ERC20Pausable, Ownable {
    uint256 private constant INCENTIVE_AMOUNT = 10;

    uint256 private tokenPrice = 1_000_000_000_000;

    mapping(address => uint256) private claimedRewards;

    AbstractSusTokenRequester private susTokenRequester;

    constructor(address _susTokenRequesterAddress)
    ERC20("Sustainability Token", "SUST")
    Ownable(_msgSender())
    {
        susTokenRequester = AbstractSusTokenRequester(_susTokenRequesterAddress);
    }

    function setTokenPrice(uint256 _tokenPrice) external onlyOwner {
        require(_tokenPrice > 0, "Invalid token price");

        tokenPrice = _tokenPrice;
    }

    function getTokenPrice() public view returns (uint256) {
        return tokenPrice;
    }

    receive() external payable {

    }

    event TokensPurchased(address indexed buyer, uint256 amount, uint256 cost);

    function purchase() public payable {
        uint256 tokenAmount = calculateTokenAmount(msg.value) / tokenPrice;
        require(tokenAmount > 0, "Not enough Ether sent");

        address buyer = _msgSender();

        _mint(buyer, tokenAmount);

        emit TokensPurchased(buyer, tokenAmount, tokenPrice);
    }

    function withdraw(address payable _recipient) external onlyOwner {
        require(_recipient != address(0), "Invalid recipient address");

        uint256 contractBalance = address(this).balance;

        require(contractBalance > 0, "No balance to transfer");

        _recipient.transfer(contractBalance);
    }

    function fetchRewards() public {
        address _recipient = _msgSender();

        susTokenRequester.makeRequest(_recipient, claimedRewards[_recipient]);

        _mint(_recipient, calculateTokenAmount(INCENTIVE_AMOUNT));
    }

    error RewardAlreadyClaimed(uint256 nonce);

    event RewardsClaimed(address indexed recipient, uint256 amount);

    function claimReward() public {
        address _recipient = _msgSender();

        ApiResponse memory pointsData = susTokenRequester.getWalletPoints(
            _recipient
        );

        uint256 pointsNonce = pointsData.lastNonce;
        uint256 claimedNonce = claimedRewards[_recipient];

        if (claimedNonce > 0 && claimedNonce >= pointsNonce) {
            revert RewardAlreadyClaimed(pointsNonce);
        }

        claimedRewards[_recipient] = pointsNonce;

        uint256 tokenAmount = calculateTokenAmount(pointsData.availablePoints);

        _mint(_recipient, tokenAmount);

        emit RewardsClaimed(_recipient, tokenAmount);
    }

    function calculateTokenAmount(uint256 _amount)
    private
    view
    returns (uint256)
    {
        return _amount * (10 ** decimals());
    }
}
