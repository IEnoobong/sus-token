import SusTokenRequester from "../artifacts/contracts/SusTokenRequester.sol/SusTokenRequester.json";
import SusToken from "../artifacts/contracts/SusToken.sol/SusToken.json";
import {
  deployContract,
  deployMockContract,
  loadFixture,
  MockProvider,
  solidity
} from "ethereum-waffle";
import {ethers} from "ethers";
import {expect, use} from "chai";

use(solidity);

describe('SusToken', function () {
  const ONE_GWEI = 1_000_000_000;

  it('should allow purchase of token', async () => {
    const {
      susToken,
      otherAccount
    } = await loadFixture(deployContractLoadFixture);
    const purchaseAmount = ONE_GWEI;
    const tokenPrice = await susToken.getTokenPrice();
    const expectedTokens = BigInt((purchaseAmount * (10 ** 18)) / tokenPrice);

    await expect(susToken.connect(otherAccount).purchase({value: purchaseAmount}))
    .to.emit(susToken, 'TokensPurchased')
    .withArgs(otherAccount.address, expectedTokens, tokenPrice);

    const balance = await susToken.provider.getBalance(susToken.address);
    expect(balance).to.be.eq(purchaseAmount)

    expect(await susToken.balanceOf(otherAccount.address)).to.be.eq(expectedTokens);
  });

  it('should revert on non-owner withdrawal', async () => {
    const {
      susToken,
      otherAccount
    } = await loadFixture(deployContractLoadFixture);

    await expect(susToken.connect(otherAccount).withdraw(otherAccount.address))
        .to.be.reverted
  });

  it('should revert on address(0) withdrawal', async () => {
    const {
      susToken,
      owner,
      otherAccount
    } = await loadFixture(deployContractLoadFixture);

    await expect(susToken.withdraw(ethers.constants.AddressZero))
    .to.be.revertedWith("Invalid recipient address")
  });

  it('should revert on withdrawal with no balance', async () => {
    const [owner, otherAccount] = new MockProvider().getWallets()

    const susTokenRequesterMock = await deployMockContract(owner, SusTokenRequester.abi);
    const susToken = await deployContract(owner, SusToken, [susTokenRequesterMock.address]);

    await expect(susToken.withdraw(otherAccount.address))
    .to.be.revertedWith("No balance to transfer");
  });

  it('should allow valid withdrawals', async () => {
    const {
      susToken,
      owner,
      otherAccount
    } = await loadFixture(deployContractLoadFixture);

    const contractInitialBalance = await susToken.provider.getBalance(susToken.address);

    //fund contract
    const tx = {
      to: susToken.address,
      value: ONE_GWEI,
    };
    await owner.sendTransaction(tx);

    const recipientInitialBalance = await otherAccount.getBalance();

    await expect(susToken.withdraw(otherAccount.address))
        .to.not.be.reverted;

    expect(await otherAccount.getBalance())
    .to.be.eq(recipientInitialBalance.add(contractInitialBalance).add(BigInt(ONE_GWEI)))
  });

  it('should fetch rewards and incentive caller', async () => {
    const {
      susToken,
      susTokenRequesterMock,
      owner,
    } = await loadFixture(deployContractLoadFixture);

    await susTokenRequesterMock.mock.makeRequest.withArgs(owner.address, 0).returns();

    await expect(susToken.fetchRewards())
        .to.not.be.reverted;

    const incentiveAmount = 10;

    const expectedTokens = BigInt(incentiveAmount * (10 ** 18));
    expect(await susToken.balanceOf(owner.address)).to.be.eq(expectedTokens);
  });

  it('should successfully claim rewards', async () => {
    const {
      susToken,
      susTokenRequesterMock, otherAccount,
    } = await loadFixture(deployContractLoadFixture);

    const availablePoints = 1000;
    await susTokenRequesterMock.mock.getWalletPoints.withArgs(otherAccount.address).returns({
      availablePoints,
      lastNonce: 1
    });

    const recipientInitialBalance = await susToken.balanceOf(otherAccount.address);
    const expectedTokens = BigInt(availablePoints * (10 ** 18));

    await expect(susToken.connect(otherAccount).claimReward())
    .to.emit(susToken, 'RewardsClaimed')
    .withArgs(otherAccount.address, expectedTokens)

    expect(await susToken.balanceOf(otherAccount.address)).to.be.eq(recipientInitialBalance.add(expectedTokens));
  });

  it('should revert for duplicate rewards claim', async () => {
    const [owner, otherAccount] = new MockProvider().getWallets()

    const susTokenRequesterMock = await deployMockContract(owner, SusTokenRequester.abi);
    const susToken = await deployContract(owner, SusToken, [susTokenRequesterMock.address]);

    const availablePoints = 1000;
    const apiResponse = {availablePoints, lastNonce: 1}

    await susTokenRequesterMock.mock.getWalletPoints.withArgs(otherAccount.address).returns(apiResponse);

    const recipientInitialBalance = await susToken.balanceOf(otherAccount.address);
    const expectedTokens = BigInt(availablePoints * (10 ** 18));

    await susToken.connect(otherAccount).claimReward();
    expect(await susToken.balanceOf(otherAccount.address)).to.be.eq(recipientInitialBalance.add(expectedTokens));

    await susTokenRequesterMock.mock.getWalletPoints.withArgs(otherAccount.address).returns(apiResponse);
    await expect(susToken.connect(otherAccount).claimReward())
        .to.be.reverted;

  });

  async function deployContractLoadFixture() {
    const [owner, otherAccount] = new MockProvider().getWallets()
    const susTokenRequesterMock = await deployMockContract(owner, SusTokenRequester.abi);

    // const SusToken = await ethers.getContractFactory("SusToken");
    const susToken = await deployContract(owner, SusToken, [susTokenRequesterMock.address]);

    return {susToken, susTokenRequesterMock, owner, otherAccount}
  }
});
