import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class disasm {
    public static void main(String args[]) {
        if(args.length < 1) {
            System.out.println("Invalid input machine file");
            return;
        }else{
            String inFile = args[0];
            ArrayList<String> insList = new ArrayList<>();
            try {
                int segCount = 0, currByte;
                FileInputStream readFile = new FileInputStream(inFile);
                String currLine = "";
                while((currByte = readFile.read()) != -1) {
                    //Basing how to do this off https://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
                    String tmp = String.format("%8s", Integer.toBinaryString(currByte & 0xFF)).replace(' ', '0');
                    currLine += tmp;
                    if(segCount != 3) {
                        segCount++;
                    } else {
                        insList.add(currLine);
                        //Reset before continuing
                        currLine = "";
                        segCount = 0;
                    }
                }
                readFile.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            DisassembleInstructions(insList);
        }
    }

    public static void DisassembleInstructions(ArrayList<String> instructionsList) {
        for(int i = 0; i < instructionsList.size(); i++) {
            String binaryCommand = instructionsList.get(i);
            String assemblyCommand = "";
            int rd = Integer.parseInt(binaryCommand.substring(27), 2);
            int rn = Integer.parseInt(binaryCommand.substring(22,27), 2);
            int shamt = Integer.parseInt(binaryCommand.substring(16,22), 2);
            int rm = Integer.parseInt(binaryCommand.substring(10,16), 2);
            int dt_Address = Integer.parseInt(binaryCommand.substring(10,20), 2);
            int aluImmediate, branchAddress, conditionalBranchAddress;
            String sixBitOperator = binaryCommand.substring(0,6);
            String eightBitOperator = binaryCommand.substring(0,8);
            String tenBitOperator = binaryCommand.substring(0,10);
            String elevenBitOperator = binaryCommand.substring(0,11);

            if(binaryCommand.charAt(10) == '1'){
                aluImmediate = Integer.parseInt(findTwosComplement(new StringBuffer(binaryCommand.substring(11,22))), 2);
                //invert sign
                aluImmediate *= -1;
            } else {
                aluImmediate = Integer.parseInt(binaryCommand.substring(11,22), 2);
            }

            if(binaryCommand.charAt(6) == '1') {
                branchAddress = Integer.parseInt(findTwosComplement(new StringBuffer(binaryCommand.substring(7))), 2);
                //invert sign
                branchAddress *= -1;
            } else {
                branchAddress = Integer.parseInt(binaryCommand.substring(7), 2);
            }

            if(binaryCommand.charAt(8) == '1') {
                conditionalBranchAddress = Integer.parseInt(findTwosComplement(new StringBuffer(binaryCommand.substring(9, 27))), 2);
                //invert sign
                conditionalBranchAddress *= -1;
            } else {
                conditionalBranchAddress = Integer.parseInt(binaryCommand.substring(9, 27), 2);
            }

            switch (sixBitOperator) {
                case "000101":
                    assemblyCommand = "B Label" + (i + branchAddress);
                    break;
                case "100101":
                    assemblyCommand = "BL Label" + (i + branchAddress);
                    break;
                default:
                    break;
            }

            switch (eightBitOperator)
            {
                case "01010100":
                    switch(rd) {
                        case 0:
                            assemblyCommand = "B.EQ Label" + (i + conditionalBranchAddress);
                            break;
                        case 1:
                            assemblyCommand = "B.NE Label" + (i + conditionalBranchAddress);
                            break;
                        case 2:
                            assemblyCommand = "B.HS Label" + (i + conditionalBranchAddress);
                            break;
                        case 3:
                            assemblyCommand = "B.LO Label" + (i + conditionalBranchAddress);
                            break;
                        case 4:
                            assemblyCommand = "B.MI Label" + (i + conditionalBranchAddress);
                            break;
                        case 5:
                            assemblyCommand = "B.PL Label" + (i + conditionalBranchAddress);
                            break;
                        case 6:
                            assemblyCommand = "B.VS Label" + (i + conditionalBranchAddress);
                            break;
                        case 7:
                            assemblyCommand = "B.VC Label" + (i + conditionalBranchAddress);
                            break;
                        case 8:
                            assemblyCommand = "B.HI Label" + (i + conditionalBranchAddress);
                            break;
                        case 9:
                            assemblyCommand = "B.LS Label" + (i + conditionalBranchAddress);
                            break;
                        case 10:
                            assemblyCommand = "B.GE Label" + (i + conditionalBranchAddress);
                            break;
                        case 11:
                            assemblyCommand = "B.LT Label" + (i + conditionalBranchAddress);
                            break;
                        case 12:
                            assemblyCommand = "B.GT Label" + (i + conditionalBranchAddress);
                            break;
                        case 13:
                            assemblyCommand = "B.LE Label" + (i + conditionalBranchAddress);
                            break;
                    }
                    break;
                case "10110100":
                    assemblyCommand = "CBZ X" + rd + ", Label" + (i + conditionalBranchAddress);
                    break;
                case "10110101":
                    assemblyCommand = "CBNZ X" + rd + ", Label" + (i + conditionalBranchAddress);
                    break;
                default:
                    //If it's anything else we don't handle it
                    break;
            }
            switch (tenBitOperator) {
                case "1001000100":
                    assemblyCommand = "ADDI X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                case "1001001000":
                    assemblyCommand = "ANDI X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                case "1101000100":
                    assemblyCommand = "SUBI X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                case "1111000100":
                    assemblyCommand = "SUBIS X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                case "1101001000":
                    assemblyCommand = "EORI X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                case "1011001000":
                    assemblyCommand = "ORRI X" + rd + ", X" + rn + ", #" + aluImmediate;
                    break;
                default:
                    //If it's anything else we don't handle it
                    break;
            }

            switch (elevenBitOperator) {
                case "11111000010":
                    assemblyCommand = "LDUR X" + rd + ", [X" + rn + ", #" + dt_Address + "]";
                    break;
                case "11111000000":
                    assemblyCommand = "STUR X" + rd + ", [X" + rn + ", #" + dt_Address + "]";
                    break;
                case "10001011000":
                    assemblyCommand = "ADD X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "10001010000":
                    assemblyCommand = "AND X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "11001010000":
                    assemblyCommand = "EOR X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "11001011000":
                    assemblyCommand = "SUB X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "10101010000":
                    assemblyCommand = "ORR X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "11101011000":
                    assemblyCommand = "SUBS X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "10011011000":
                    assemblyCommand = "MUL X" + rd + ", X" + rn + ", X" + rm;
                    break;
                case "11010011011":
                    assemblyCommand = "LSL X" + rd + ", X" + rn + ", #" + shamt;
                    break;
                case "11010011010":
                    assemblyCommand = "LSR X" + rd + ", X" + rn + ", #" + shamt;
                    break;
                case "11010110000":
                    assemblyCommand = "BR X" + rn;
                    break;
                case "11111111101":
                    assemblyCommand = "PRNT X" + rd;
                    break;
                case "11111111100":
                    assemblyCommand = "PRNL";
                    break;
                case "11111111110":
                    assemblyCommand = "DUMP";
                    break;
                case "11111111111":
                    assemblyCommand = "HALT";
                    break;
                default:
                    //If it's anything else we don't handle it
                    break;
            }
            //print out parsed command alongside what number it is, etc.
            System.out.println("Label " + i + ": " + assemblyCommand);
        }
    }

    //Derived from https://www.geeksforgeeks.org/efficient-method-2s-complement-binary-string/
    static String findTwosComplement(StringBuffer line) {

        //Find first 1
        int first1Pos;
        for (first1Pos = (line.length() - 1); first1Pos >= 0 ; first1Pos--) {
            if (line.charAt(first1Pos) == '1') {
                break;
            }
        }
        if (first1Pos == -1){
            return "1" + line;
        }
        // Continue traversal after the position of the first 1 found
        for (int i = first1Pos - 1; i >= 0; i--)
        {
            //switcg
            if (line.charAt(i) == '1'){
                line.replace(i, i + 1, "0");
            } else{
                line.replace(i, i + 1, "1");
            }
        }
        return line.toString();
    }
}
