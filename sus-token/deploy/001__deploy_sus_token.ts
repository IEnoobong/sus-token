import {HardhatRuntimeEnvironment} from "hardhat/types";
import {DeployFunction} from "hardhat-deploy/dist/types";
import {required} from "../index";

const func: DeployFunction = async function (hre: HardhatRuntimeEnvironment) {
  const {deploy} = hre.deployments;
  const {deployer} = await hre.getNamedAccounts();

  const {
    address: susTokenRequesterAddress,
    abi: susTokenRequesterAbi
  } = await deploy('SusTokenRequester', {
    from: deployer,
    args: [required(<string>process.env[`AIRNODE_${hre.network.name.toUpperCase()}_RRP`])]
  });

  console.log('SusTokenRequester deployed at', susTokenRequesterAddress);

  const susTokenRequester = await hre.ethers.getContractAt(susTokenRequesterAbi, susTokenRequesterAddress);
  await susTokenRequester.setAirnodeData({
    endpointId: <string>process.env.AIRNODE_ENDPOINT_ID,
    airnode: <string>process.env.AIRNODE_ADDRESS,
    sponsor: <string>process.env.SPONSOR_ADDRESS,
    sponsorWallet: <string>process.env.SPONSOR_WALLET_ADDRESS,
  });

  const {address: susTokenAddress} = await deploy('SusToken', {
    from: deployer,
    args: [susTokenRequesterAddress]
  });

  console.log('SusToken deployed at', susTokenAddress);
};

export default func;
